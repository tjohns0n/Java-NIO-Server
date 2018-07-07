package cs455.scaling.client;

import cs455.scaling.message.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class Client {

    final private SocketChannel serverConnectionChannel;
    final private InetAddress serverAddress;
    final private int serverPort;
    final private LinkedList<String> hashes;
    final private int messageRate;
    private Integer totalSentCount = 0;
    private Integer totalReceivedCount = 0;
    private final Object counterLock = new Object();
    /**
     * Get the port the connected server runs on
     * @return  -1 if connection setup failed,
     *          the server port if connection succeeds
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Create a new client that connects to a cs455.scaling.server.Server
     * @param serverAddress Address server is running on
     * @param serverPort    Port server is running on
     * @param messageRate   Rate at which to send messages to the Server (1/messageRate per second)
     */
    public Client(InetAddress serverAddress, int serverPort, int messageRate) {
        hashes = new LinkedList<>();
        this.messageRate = 1000 / messageRate;
        serverConnectionChannel = connectToServer(serverAddress, serverPort);
        this.serverAddress = serverAddress;
        if (serverConnectionChannel == null) {
            this.serverPort = -1;
        } else {
            this.serverPort = serverPort;
        }
    }

    /**
     * Attempt to connect to a Server
     * @param serverAddress Address server is running on
     * @param serverPort    Port server is running on
     * @return              On success, a valid SocketChannel connecting to the server
     *                      On failure, null
     */
    private SocketChannel connectToServer(InetAddress serverAddress, int serverPort) {
        SocketChannel tempChannel;
        try {
            tempChannel = SocketChannel.open();
            tempChannel.connect(new InetSocketAddress(serverAddress, serverPort));
            tempChannel.configureBlocking(false);
        } catch (IOException e) {
            System.err.println("Client.class: Could not connect to server");
            e.printStackTrace();
            tempChannel = null;
        }
        return tempChannel;
    }

    /**
     * Start sending messages at messageRate to the server. Also read messages received back from the Server
     * @throws InterruptedException on interrupt
     * @throws IOException if connection fails
     */
    public void start() throws InterruptedException, IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8192);
        ByteBuffer readBuffer = ByteBuffer.allocateDirect(40);
        while (true) {
            byteBuffer.clear();
            Message message = new Message();
            byteBuffer.put(message.getRawBytes());
            byteBuffer.flip();
            int written = 0;
            synchronized (counterLock) {
                totalSentCount++;
            }
            hashes.add(message.getHash().getHash());
            while (written < 8192) {
                written += serverConnectionChannel.write(byteBuffer);
            }
            int read = 0;
            while (read < 40) {
                readBuffer.clear();
                int oneRead = serverConnectionChannel.read(readBuffer);
                if (oneRead != 0) {
                    read += oneRead;
                    while (read != 40) {
                        read += serverConnectionChannel.read(readBuffer);
                    }
                    synchronized (counterLock) {
                        totalReceivedCount++;
                    }
                    byte[] hashArray = new byte[40];
                    readBuffer.flip();
                    readBuffer.get(hashArray);
                    String hash = new String(hashArray);
                    hashes.remove(hash);
                    read = 0;
                } else {
                    read = 40;
                }
            }
            Thread.sleep(messageRate);
        }
    }

    public Report getReport() {
        synchronized (counterLock) {
            Report report = new Report(totalSentCount, totalReceivedCount);
            totalSentCount = 0;
            totalReceivedCount = 0;
            return report;
        }
    }

    public static void main(String args[]) {
        InetAddress serverAddress;
        int serverPort;
        int messageRate;
        if (args.length < 3) {
            System.err.println("Required arguments: [server address] [server port] [message rate]");
            return;
        } else {
            try {
                serverAddress = InetAddress.getByName(args[0]);
                serverPort = Integer.parseInt(args[1]);
                messageRate = Integer.parseInt(args[2]);
            } catch (NumberFormatException nfe) {
                System.err.println("Server port and message rate must be specified as numbers");
                return;
            } catch (UnknownHostException uhe) {
                System.err.println("Server by name " + args[0] + " does not exist");
                return;
            }
        }
        Client client = new Client(serverAddress, serverPort, messageRate);
        if (client.getServerPort() > 0) {
            try {
                new Thread(new Reporter(client)).start();
                client.start();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

        }
    }



}
