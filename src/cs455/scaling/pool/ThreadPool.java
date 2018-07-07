package cs455.scaling.pool;

import cs455.scaling.utils.BlockingLinkedList;
import cs455.scaling.work.Work;

/**
 * <p>A basic implementation of a thread pool. Currently only schedules objects that extend the Work
 * superclass, but with minor modification it would work with any Runnable object. The thread pool implements
 * runnable, so the manager can run on a dedicated thread or as the master thread.</p>
 * <p>Child classes of Work are allowed to create additional Work objects that are scheduled for execution.</p>
 */
public class ThreadPool implements Runnable {

    private final ThreadPoolManager manager;

    /**
     * Initiate a thread pool
     * @param poolSize the size of the thread pool
     * @param workObjects Objects that extend Work that should be executed when the thread pool starts.
     *                    Additional Work can be registered after the pool has started with the
     */
    public ThreadPool(int poolSize, Work... workObjects) {
        manager = new ThreadPoolManager(poolSize, workObjects);
    }

    /**
     * Start the manager, which in turn starts all of the pool's threads.
     */
    public void run() {
        manager.run();
    }

    /**
     * Allow outside objects to schedule new tasks for execution.
     * @param workObjects The Work tasks that should be scheduled
     */
    public void registerWork(Work... workObjects) {
        manager.registerWork(workObjects);
    }

    /**
     * Manages the ThreadPool. Assigns threads jobs that extend the Work class.
     */
    private static class ThreadPoolManager implements Runnable {

        final private int poolSize;

        // Equal to the poolSize minus the number of threads involved in infinite tasks
        private int registerableThreads;

        final private BlockingLinkedList<Work> work;
        final private BlockingLinkedList<WorkerThread> threads;

        /**
         * Create a ThreadPoolManager. The run method initiates the pool's threads
         *
         * @param poolSize    The number of threads the pool should have
         * @param workObjects Required Work that is known at construction. Can be null if there is none.
         */
        ThreadPoolManager(int poolSize, Work... workObjects) {
            this.poolSize = poolSize;
            registerableThreads = poolSize;
            work = new BlockingLinkedList<>();
            threads = new BlockingLinkedList<>();
            registerWork(workObjects);
        }

        /**
         * Attempt to Work objects to the work list the thread pool is operating on
         * Registration will fail if there are not enough threads to handle the request
         * Registration of Work with threadsNeeded = 0 will always succeed, but too registering too
         * many may strain the thread pool. Failed registration will print an error, but it
         * will not throw an exception, and the thread pool will continue doing its registered Work
         *
         * @param workObjects The Work object(s) to be registered
         */
        synchronized void registerWork(Work... workObjects) {
            if (workObjects == null) {
                return;
            }
            for (Work work : workObjects) {
                int requestedThreads = work.getThreadsNeeded();
                if (requestedThreads > registerableThreads) {
                    System.err.println("There are not enough threads available for the requested task!");
                    return;
                } else {
                    this.work.add(work);

                    registerableThreads = registerableThreads - requestedThreads;
                }
            }
        }

        /**
         * Start poolSize threads, adding them to the manager's thread list
         */
        void startThreads() {
            for (int i = 0; i < poolSize; i++) {
                WorkerThread workerThread = new WorkerThread(this);
                queueThread(workerThread);
                new Thread(workerThread).start();
            }
        }

        /**
         * Notify the WorkerThread at the front of the queue when there is work to be done,
         * waiting for a thread or work to become available if necessary
         */
        void assignWork() {
            try {
                WorkerThread workerThread = threads.take();
                Work job = work.take();
                workerThread.assign(job);
                synchronized (workerThread) {
                    workerThread.notify();
                }
            } catch (InterruptedException e) {
                System.err.println("ThreadPoolManager: interrupted");
            }
        }

        /**
         * Add a thread to the queue. Called by this at the start of its run method as well as by WorkerThreads
         * that have finished their assigned job.
         *
         * @param workerThread
         */
        void queueThread(WorkerThread workerThread) {
            threads.add(workerThread);
        }

        @Override
        public void run() {
            startThreads();
            while (true) {
                assignWork();
            }
        }
    }

    /**
     * A wrapper object for the threads used by a ThreadPoolManager. The WorkerThread objects
     * themselves are not thread safe, but the implementation of ThreadPoolManager handles
     * concurrency and is thread safe.
     */
    private static class WorkerThread implements Runnable {

        // The pool manager that invoked this thread
        final private ThreadPoolManager manager;
        // The current task to complete;
        private Work currentJob;

        /**
         * Create a WorkerThread (should only be called by the ThreadPoolManager
         * @param manager
         */
        WorkerThread(ThreadPoolManager manager) {
            this.manager = manager;
        }

        /**
         * Called by the ThreadPoolManager to give the thread work
         *
         * @param job The Work object to invoke the run method on
         */
        void assign(Work job) {
            currentJob = job;
        }

        /**
         * Wait until ThreadPoolManager sends a notification that this thread has been assigned work
         *
         * @throws InterruptedException
         */
        synchronized void waitForWork() throws InterruptedException {
            wait();
        }

        /**
         * Put self back on the ThreadPoolManager's queue on job completion
         */
        void returnToPool() {
            manager.queueThread(this);
        }

        /**
         * Wait on and do work until interrupted
         */
        public void run() {
            while (true) {
                try {
                    waitForWork();
                    currentJob.run();
                    returnToPool();
                } catch (InterruptedException e) {
                    System.err.println("WorkThread: Interrupted");
                }
            }
        }

    }


}

