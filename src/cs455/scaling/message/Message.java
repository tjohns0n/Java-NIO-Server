package cs455.scaling.message;

import java.math.BigInteger;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * A random 8 KB message. Used for testing Client-Server scalability.
 */
public class Message {

    final private static Random random = new Random();

    // The bytes of the message, which will be sent from a client to the server
    final private byte[] rawBytes;

    // Where the message came from (Only used on Server)
    final private SocketAddress source;

    /**
     * Client-side constructor. Creates a random 8 KB message
     */
    public Message() {
        rawBytes = new byte[8192];
        random.nextBytes(rawBytes);
        source = null;
    }

    /**
     * Server-side constructor. Reconstructs the message a client sent, with information about the client
     * @param rawBytes An 8KB byte array
     * @param sourceAddress The SocketAddress of the client that sent the original message
     */
    public Message(byte[] rawBytes, SocketAddress sourceAddress) {
        this.rawBytes = rawBytes;
        source = sourceAddress;
    }

    /**
     * Get the address of the object that sent the message (only used by server)
     * @return
     */
    public SocketAddress getSource() {
        return source;
    }

    /**
     * Get the message as a byte array
     * @return The byte array representing the message
     */
    public byte[] getRawBytes() {
        return rawBytes;
    }


    /**
     * Return a SHA-1 hash of the bytes of a Message
     * @return the hash wrapped in a HashMessage object
     * Referenced http://www.javased.com/index.php?api=java.security.MessageDigest for a method of creating a hash String
     * that does not drop 0 bits
     */
    public HashMessage getHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(rawBytes,0, rawBytes.length);
            byte[] bytes = md.digest();
            return new HashMessage(String.format("%0" + (bytes.length << 1) + "x", new BigInteger(1, bytes)));
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Message.class: Algorithm does not exist");
            return null;
        }
    }

    public static void main(String args[]) {
        Message message = new Message();
        HashMessage h = message.getHash();
        System.out.println(h.getHash().length());
    }

}
