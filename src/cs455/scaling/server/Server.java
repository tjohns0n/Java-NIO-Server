package cs455.scaling.server;

import cs455.scaling.work.SelectorWork;
import cs455.scaling.pool.ThreadPool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {

    final private int port;
    final private int poolSize;
    final private ServerSocketChannel serverSocketChannel;
    final private Selector selector;
    final private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Return the port the server is running on.
     * @return  The value of the Server's port variable if it is running,
     *          0 if the Server hasn't been started
     *          -1 if the Server setup fails
     */
    public int getServerPort() {
        return port;
    }

    /**
     * Create a Server object.
     * It is not guaranteed the server will open on the specified port if it is in use
     * @param port      The desired port to run the Server on
     * @param poolSize  Size of the thread pool that will handle server jobs
     */
    public Server(int port, int poolSize) {
        selector = openSelector();

        // Set size of thread pool
        this.poolSize = poolSize;

        // Create nio server
        serverSocketChannel = openServerSocketChannel();
        if (serverSocketChannel == null || selector == null) {
            this.port = -1;
            return;
        }
        // Bind to port
        this.port = bindServerSocketChannel(port);
    }

    /**
     * Open a Selector
     * @return an opened Selector on success, null on failure
     */
    private Selector openSelector() {
        Selector tempSelector;
        try {
            tempSelector = Selector.open();
            return tempSelector;
        } catch (IOException e) {
            System.err.println("Server.class: Could not create Selector");
            return null;
        }
    }

    /**
     * Open a ServerSocketChannel and return a reference to it.
     * @return      an opened ServerSocketChannel on success, null on failure
     */
    private ServerSocketChannel openServerSocketChannel() {
        ServerSocketChannel tempChannel;
        try {
            tempChannel = ServerSocketChannel.open();
        } catch (IOException e) {
            System.err.println("Server.class: Failed to create ServerSocketChannel");
            tempChannel = null;
        }
        return tempChannel;
    }

    /**
     * Bind the Server class' serverSocketChannel to a port.
     * @param port  The desired port
     * @return      The port the server actually started on
     */
    private int bindServerSocketChannel(int port) {
        while (true) {
            try  {
                serverSocketChannel.socket().bind(new InetSocketAddress(port));
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                return port;
            } catch (IOException e) {
                if (port < 65535) {
                    port++;
                } else { // Loop back to bottom of range if port not found
                    port = 1024;
                }
            }
        }
    }

    /**
     * Start up a thread pool that handles the rest of the server's operations
     */
    public void startServer() throws InterruptedException {
        ThreadPool threadPool = new ThreadPool(poolSize);
        SelectorWork selectorWork = new SelectorWork(selector, serverSocketChannel, threadPool);
        threadPool.registerWork(selectorWork);
        Thread threadPoolThread = new Thread(threadPool);
        threadPoolThread.start();
        while (true) {
            Thread.sleep(20000);
            Report report = selectorWork.getReport();
            System.out.printf("[" + dtf.format(LocalDateTime.now()) + "] Server Throughput: %f messages/s, Active Client Connections: %d, Mean Per-" +
                    "client Throughput: %f messages/s, Std. Dev. Of Per" + "-client Throughput: %f messages/s\n",
                    report.totalMessagesPerSecond, report.machinesRegistered, report.averageMessagesPerSecond, report.messageStdDev);
        }

    }

    public static void main(String args[]) {
        int port, poolSize;
        if (args.length < 2) {
            System.err.println("Must specify arguments: [port] [thread pool size]");
            return;
        } else {
            try {
                port = Integer.parseInt(args[0].trim());
                poolSize = Integer.parseInt(args[1].trim());
            } catch (NumberFormatException e) {
                System.err.println("Port and pool size must be a number");
                return;
            }
        }

        Server server = new Server(port, poolSize);

        if (server.getServerPort() > 0) {
            try {
                System.out.println("Server started on host: " + InetAddress.getLocalHost() + " port: " + port);
                server.startServer();
            } catch (UnknownHostException e) {
                System.err.println("Server.class: No such localhost");
            } catch (InterruptedException e) {
                System.err.println("Server: interrupted");
            }
        } else {
            System.err.println("Server.class: Setup of server could not complete. Exiting...");
        }
    }


}
