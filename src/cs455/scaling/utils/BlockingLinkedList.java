package cs455.scaling.utils;

import java.util.LinkedList;

/**
 * <p>
 *     A thread safe wrapper class for a LinkedList. Not all
 *     LinkedList methods are available, but the class grants:
 *     <ul>
 *         <li>Thread safety</li>
 *         <li>The uniqueness of a Set</li>
 *         <li>Polling operations for removing the first element</li>
 *     </ul>
 * </p><p>
 *
 * </p>
 * @param <T> The class of objects the BlockingLinkedList should hold
 */
public class BlockingLinkedList<T> {

    private final LinkedList<T> linkedList;

    public BlockingLinkedList() {
        linkedList = new LinkedList<>();
    }

    /**
     * Appends an item to the end of the linked list if it is not present
     * @param t The object to append
     * @return True on success and false on failure
     */
    public boolean add(T t) {
        synchronized (linkedList) {
            if (!linkedList.contains(t)) {
                boolean returnVal = linkedList.add(t);
                // On success, notify threads waiting on take() that an element has been added
                if (returnVal) {
                    linkedList.notifyAll();
                }
                return returnVal;
            }
            return false;
        }
    }

    /**
     * Retrieve and remove the first element of the list if there is one
     * @return The first element of the list or null if the list is empty
     */
    public T poll() {
        if (linkedList.size() == 0) {
            return null;
        } else {
            synchronized (linkedList) {
                if (linkedList.size() == 0) {
                    return null;
                } else {
                    return linkedList.remove();
                }
            }
        }
    }

    /**
     * Retrieve and remove the first element of the list, waiting if necessary
     * @return The first element of the list
     * @throws InterruptedException on interrupt
     */
    public T take() throws InterruptedException {
        while (true) {
            // If multiple threads are accessing the linked list, make sure the list actually contains an element:
            synchronized (linkedList) {
                if (linkedList.size() > 0) {
                    return linkedList.remove();
                } else {
                    linkedList.wait();
                    if (linkedList.size() > 0) {
                        return linkedList.remove();
                    }
                }
            }
        }
    }

    /**
     * Return the size of the underlying LinkedList.
     * @return
     */
    public int size() {
        synchronized (linkedList) {
            return linkedList.size();
        }
    }



}
