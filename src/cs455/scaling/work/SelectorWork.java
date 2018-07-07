package cs455.scaling.work;

import cs455.scaling.message.HashMessage;
import cs455.scaling.message.Message;
import cs455.scaling.pool.ThreadPool;
import cs455.scaling.server.Report;
import cs455.scaling.server.ReportCounter;
import cs455.scaling.utils.BlockingLinkedList;
import cs455.scaling.utils.SafeArrayList;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * <p>Perform work in the Thread pool associated with a single Selector. It requires 1 thread to run, and
 * it will not release the Thread until the Selector is closed.</p>
 * <p>A SelectorWork object will add ScalingMessageWork to the thread pool and occasionally check on its progress.
 * If the ScalingMessageWork object has completed its run method, the SelectorWork object will add its
 * HashMessage to its writing queue and will eventually write the result to the proper socket.</p>
 * <p>The SelectorWork object is not thread safe, so only its corresponding ThreadPoolManager should
 * hold a reference to it.</p>
 */
public class SelectorWork extends Work implements HashCommunication {

    // A Selector for the server's serverChannel and the connections it accepts
    private final Selector selector;
    // The channel the server is listening on for incoming connections
    private final ServerSocketChannel serverChannel;
    // A queue of byte[]s that need processed. Each byte[] will be 8KB so it can be converted to a Message
    private BlockingLinkedList<byte[]> readyForProcessing;
    // A queue of HashMessages that need to be written to the socket that sent the original Message.
    private SafeArrayList<HashMessage> readyForWrite;
    // The thread pool SelectorWork should add jobs to
    private final ArrayList<ReportCounter> counters;
    private ThreadPool threadPool;

    /**
     * @param selector The selector to which the serverChannel will be registered
     * @param serverChannel The channel that listens for incoming connections on the server
     * @param threadPool The thread pool this object should add work to
     */
    public SelectorWork(Selector selector, ServerSocketChannel serverChannel, ThreadPool threadPool) {
        threadsNeeded = 1;
        this.selector = selector;
        this.serverChannel = serverChannel;
        this.threadPool = threadPool;

        readyForProcessing = new BlockingLinkedList<>();
        readyForWrite = new SafeArrayList<>();

        counters = new ArrayList<>(100);
    }

    /**
     * Generate a report of current connections and throughput. Should be called every 20 seconds
     * @return A report containing said info
     */
    public Report getReport() {
        Report report;
        synchronized (counters) {
            report = new Report(counters);
            for (ReportCounter counter : counters) {
                counter.reset();
            }
        }
        return report;
    }

    /**
     * Accepts a connection from a client and registers the corresponding SocketChannel to the Selector
     * @throws IOException Thrown if the object's selector is closed
     */
    private void registerToSelector() throws IOException {
        try {
            SocketChannel newChannel = serverChannel.accept();
            newChannel.configureBlocking(false);
            newChannel.register(selector, SelectionKey.OP_READ);
            synchronized (counters) {
                counters.add(new ReportCounter(newChannel.getRemoteAddress()));
            }
        } catch (ClosedChannelException e) {
            System.err.println("SelectorWork: Could not register channel to selector: Channel is closed");
        }
    }

    /**
     * <p>Read a message from the channel.</p>
     * <p>Side effects: Message objects will wrapped in a ScalingMessageWork object and added to the Work queue of the thread pool,
     * and the interest OPs for the key will be set to WRITE</p>
     * @param key The key that contains the SocketChannel to read from (passing a Channel that is not a SocketChannel may cause problems)
     */
    private void readFromChannel(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel)key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
        while (byteBuffer.hasRemaining()) {
            channel.read(byteBuffer);
        }
        Message message = new Message(byteBuffer.array(), channel.getRemoteAddress());
        threadPool.registerWork(new ScalingMessageWork(this::communicate, message));
        key.interestOps(SelectionKey.OP_WRITE);
    }

    /**
     * Write hashes from the object's hash list that should be sent to the object in question
     * @param key The key that contains the SocketChannel to write to
     */
    private void writeToChannel(SelectionKey key) throws InterruptedException, IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(40);
        HashMessage h = new HashMessage();
        SocketAddress address = channel.getRemoteAddress();
        h.setReturnAddress(address);
        ArrayList<HashMessage> messages = readyForWrite.getAndRemoveAll(h);

        for (HashMessage hash : messages) {
            byteBuffer.clear();
            byteBuffer.put(hash.getHash().getBytes());
            byteBuffer.flip();
            int written = 0;
            while (written < 40) {
                written += channel.write(byteBuffer);
            }
            synchronized (counters) {
                counters.get(counters.indexOf(new ReportCounter(address))).increment();
            }
        }

        key.interestOps(SelectionKey.OP_READ);
    }

    /**
     * This run method will be called by a Thread in the ThreadPool managed by the ThreadPoolManager this object
     * was registered to.
     */
    @Override
    public void run() {
        while (true) {
            try {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                // Iterate over the channels that are ready for IO.
                while (iterator.hasNext()) {
                    SelectionKey selection = iterator.next();
                    // Can only be the serverChannel held by this object
                    if (selection.isAcceptable()) {
                        registerToSelector();
                    }

                    // Is a connection accepted by the serverChannel
                    if (selection.isReadable()) {
                        readFromChannel(selection);
                    }

                    if (selection.isWritable()) {
                        writeToChannel(selection);
                    }

                    iterator.remove();
                }
            } catch (IOException e) {
                System.err.println("SelectorWork: IOException from Selector");
            } catch (InterruptedException e) {
                System.err.println("SelectorWork: Interrupted");
            }
        }
    }

    @Override
    public void communicate(ArrayList<HashMessage> hash) {
        readyForWrite.addAll(hash);
    }
}
