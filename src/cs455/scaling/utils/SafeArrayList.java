package cs455.scaling.utils;

import java.util.ArrayList;

/**
 * A minimal wrapper for ArrayLists to provide thread safety. Only contains addAll and removeAll functionality.
 * @param <T> The object to maintain in the list
 */
public class SafeArrayList<T> {

    private ArrayList<T> list;

    /**
     * Create an empty ArrayList
     */
    public SafeArrayList() {
        list = new ArrayList<>();
    }

    /**
     * Add every element of an ArrayList to the list
     * @param t A list of elements to add
     */
    public synchronized void addAll(ArrayList<T> t) {
        synchronized (list) {
            list.addAll(t);
        }
    }

    /**
     * Removes all matching objects from the list and returns them
     * @param t the object to look for
     * @return The sub-list removed from the list
     */
    public synchronized ArrayList<T> getAndRemoveAll(T t) {
        ArrayList<T> returnList = new ArrayList<>();
        synchronized (list) {
            for (T item : list) {
                if (item.equals(t)) {
                    returnList.add(item);
                }
            }
            list.removeAll(returnList);
        }
        return returnList;
    }

    public synchronized String toString() {
        return list.toString();
    }

}
