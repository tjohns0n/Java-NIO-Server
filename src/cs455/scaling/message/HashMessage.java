package cs455.scaling.message;

import java.net.SocketAddress;
// TODO: Move from SocketAddress to InetSocketAddress

/**
 * Holds a SHA-1 hash of a Message and the address it needs to be sent to
 */
public class HashMessage {

    private final String hash;
    private SocketAddress returnAddress;

    public HashMessage(String hash) {
        this.hash = hash;
    }
    public HashMessage() {hash = "";}

    /**
     * Set the address the message needs to go to. This can only be done once.
     * @param address The SocketAddress of the SocketChannel
     * @return True on first call, false on consecutive calls
     */
    public boolean setReturnAddress(SocketAddress address) {
        if (returnAddress == null) {
            returnAddress = address;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the SHA-1 hash
     * @return
     */
    public String getHash() {
        return hash;
    }

    /**
     * Checks if two HashMessages have the same return address. Different hashes will still return true.
     * @param o The object for comparison
     * @return True if the return addresses match, false if they do not or if o is not a HashMessage
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof HashMessage) {
            if (returnAddress.equals(((HashMessage) o).returnAddress)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return hash;
    }
}
