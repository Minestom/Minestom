package net.minestom.server.lock;

import net.minestom.server.thread.BatchThread;
import net.minestom.server.thread.batch.BatchSetupHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Represents an element which can be acquired.
 * Used for synchronization purpose.
 *
 * @param <T> the acquirable object type
 */
public interface AcquirableElement<T> {

    @NotNull
    default void acquire(@NotNull Consumer<T> consumer) {
        AcquisitionLock acquisitionLock = new AcquisitionLock();

        boolean sameThread = getHandler().tryAcquisition(acquisitionLock);
        final T unwrap = unsafeUnwrap();
        if (sameThread) {
            consumer.accept(unwrap);
        } else {
            synchronized (unwrap) {
                consumer.accept(unwrap);
                // Notify the end of the task
                Phaser phaser = acquisitionLock.getPhaser();
                if (phaser != null) {
                    phaser.arriveAndDeregister();
                }
            }
        }
    }

    @NotNull
    T unsafeUnwrap();

    @NotNull
    Handler getHandler();

    class Handler {

        /**
         * Notifies all the lock and wait for them to return using a {@link Phaser}.
         * <p>
         * Currently called during entities tick (TODO: chunks & instances)
         * and in {@link BatchThread.BatchRunnable#run()} after every thread-tick.
         *
         * @param acquisitionQueue the queue to empty containing the locks to notify
         */
        public static void processQueue(@NotNull Queue<AcquisitionLock> acquisitionQueue) {
            AcquisitionLock lock;
            while ((lock = acquisitionQueue.poll()) != null) {
                Phaser phaser = new Phaser(1);
                //System.out.println("NOTIFY " + acquisitionQueue.hashCode());
                synchronized (lock) {
                    synchronized (ACQUIRABLE_LOCK) {
                        //System.out.println("end modify");
                        lock.setPhaser(phaser);
                        phaser.register();
                        lock.notifyAll();
                    }
                }
        
                // Wait for the acquisitions to end
                phaser.arriveAndAwaitAdvance();
            }
        }

        private static final Object ACQUIRABLE_LOCK = new Object();
        private volatile BatchThread batchThread = null;

        /**
         * Checks if the {@link AcquirableElement} update tick is in the same thread as {@link Thread#currentThread()}.
         * If yes return immediately, otherwise a lock will be created and added to {@link BatchThread#getWaitingAcquisitionQueue()}
         * to be executed later during {@link #processQueue(Queue)}.
         *
         * @param lock the lock used if a thread-mismatch is found
         * @return true if the acquisition didn't require any synchronization
         */
        public boolean tryAcquisition(@NotNull AcquisitionLock lock) {
            final Queue<AcquisitionLock> periodQueue = getPeriodQueue();

            final Thread currentThread = Thread.currentThread();
            final boolean isBatchThread = currentThread instanceof BatchThread;

            final boolean differentThread = !isBatchThread ||
                    (batchThread != null && System.identityHashCode(batchThread) != System.identityHashCode(currentThread));

            if (differentThread) {

                synchronized (lock) {
                    synchronized (ACQUIRABLE_LOCK) {
                        Queue<AcquisitionLock> currentQueue = ((BatchThread) currentThread).getWaitingAcquisitionQueue();
                        //System.out.println("HERE");
                        //System.out.println("we got here");
                        processQueue(currentQueue);
                        //System.out.println("pre " + currentQueue.size());

                        periodQueue.add(lock);

                        lock.setBlockedThread(currentThread);

                        //System.out.println("pre2 " + currentQueue.size());

                    /*System.out.println("ADD WAIT " + currentThread.getName() + " " +
                            periodQueue.hashCode() + " " + periodQueue.size() + " " +
                            currentQueue.hashCode() + " " +
                            currentQueue.size());*/
                    }

                    // FIXME: here can be called processQueue(), notifying the lock before the wait call
                    // FIXME: two threads can be here, meaning that those two threads can both wait on each other

                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                //System.out.println("pre2 " + currentQueue.size());

                    /*System.out.println("ADD WAIT " + currentThread.getName() + " " +
                            periodQueue.hashCode() + " " + periodQueue.size() + " " +
                            currentQueue.hashCode() + " " +
                            currentQueue.size());*/

                return false;
            } else {
                //System.out.println("GOOD");
                return true;
            }
        }

        /**
         * Specifies in which thread this element will be updated.
         * Currently defined before every tick for all game elements in {@link BatchSetupHandler#pushTask(List, long)}.
         *
         * @param batchThread the thread where this element will be updated
         */
        public void refreshThread(@NotNull BatchThread batchThread) {
            this.batchThread = batchThread;
        }

        /**
         * Executed during this element tick to empty the current thread acquisition queue.
         */
        public void acquisitionTick() {
            processQueue(batchThread.getWaitingAcquisitionQueue());
        }

        /**
         * Gets the acquisition queue linked to this element's thread.
         *
         * @return the acquisition queue
         */
        public Queue<AcquisitionLock> getPeriodQueue() {
            return batchThread != null ? batchThread.getWaitingAcquisitionQueue() : null;
        }
    }

    final class AcquisitionLock {

        private volatile Phaser phaser;
        private volatile Thread blockedThread;

        @Nullable
        public Phaser getPhaser() {
            return phaser;
        }

        public void setPhaser(@NotNull Phaser phaser) {
            this.phaser = phaser;
        }

        public Thread getBlockedThread() {
            return blockedThread;
        }

        public void setBlockedThread(Thread blockedThread) {
            this.blockedThread = blockedThread;
        }
    }

}
