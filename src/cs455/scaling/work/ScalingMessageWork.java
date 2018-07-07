package cs455.scaling.work;

import cs455.scaling.message.HashMessage;
import cs455.scaling.message.Message;

import java.util.ArrayList;

/**
 * <p>Takes one or more 8 KB byte arrays, produces SHA-1 hashes of them,
 * and adds the hashes to a SelectorWork object's write queue</p>
 *
 * <p>Intended for use by SelectorWork</p>
 */
public class ScalingMessageWork extends Work {

    private final Message[] messages;
    private final HashCommunication addToSelectorWork;

    /**
     * Initialize the work
     * @param addToSelectorWork A reference to the SelectorWork that instantiated the class (implements the HashCommunication
     *                          interface for thread safety)
     * @param messages The messages to be hashed
     */
    ScalingMessageWork(HashCommunication addToSelectorWork, Message... messages) {
        this.messages = new Message[messages.length];
        for (int i = 0; i < messages.length; i++) {
            this.messages[i] = messages[i];
        }
        this.addToSelectorWork = addToSelectorWork;

    }

    /**
     * Loop through the messages array, generate their hashes, and then send them as a list back to the SelectorWork object
     */
    @Override
    public void run() {
        ArrayList<HashMessage> hashList = new ArrayList<>();
        for (Message m : messages) {
            HashMessage h = m.getHash();
            h.setReturnAddress(m.getSource());
            hashList.add(h);
        }
        addToSelectorWork.communicate(hashList);
    }

}
