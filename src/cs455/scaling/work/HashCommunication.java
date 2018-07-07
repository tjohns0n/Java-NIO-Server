package cs455.scaling.work;

import cs455.scaling.message.HashMessage;

import java.util.ArrayList;

/**
 * Used for passing hashes between objects. Intended to reduce the scope of object references
 */
public interface HashCommunication {

    /**
     * Send a hash to another object
     * @param hash The hash to send
     */
    public void communicate(ArrayList<HashMessage> hash);
}
