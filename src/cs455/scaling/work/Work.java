package cs455.scaling.work;

/**
 * Interface to define work for cs455.scaling.pool.ThreadPool
 */
public abstract class Work implements Runnable {

    /**
     * The number of threads the Work requires.
     * Use 0 to request as many threads as possible
     * Otherwise, use an integer > 0 that the Work needs in the thread pool
     */
    protected int threadsNeeded = 0;
    public int getThreadsNeeded() {
        return threadsNeeded;
    }

}
