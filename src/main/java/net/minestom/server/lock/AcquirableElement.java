package net.minestom.server.lock;

import net.minestom.server.thread.BatchThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        public static void processQueue(@NotNull Queue<AcquisitionLock> acquisitionQueue) {
            synchronized (ACQUIRABLE_LOCK) {
                AcquisitionLock lock;
                Phaser phaser = new Phaser(1);
                while ((lock = acquisitionQueue.poll()) != null) {
                    //System.out.println("NOTIFY " + acquisitionQueue.hashCode());
                    synchronized (lock) {
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

                        //System.out.println("pre2 " + currentQueue.size());

                    /*System.out.println("ADD WAIT " + currentThread.getName() + " " +
                            periodQueue.hashCode() + " " + periodQueue.size() + " " +
                            currentQueue.hashCode() + " " +
                            currentQueue.size());*/
                    }
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

        public void refreshThread(@NotNull BatchThread batchThread) {
            this.batchThread = batchThread;
        }

        public void acquisitionTick() {
            processQueue(batchThread.getWaitingAcquisitionQueue());
        }

        public Queue<AcquisitionLock> getPeriodQueue() {
            return batchThread != null ? batchThread.getWaitingAcquisitionQueue() : null;
        }
    }

    class AcquisitionLock {

        private volatile Phaser phaser;

        @Nullable
        public Phaser getPhaser() {
            return phaser;
        }

        public void setPhaser(@NotNull Phaser phaser) {
            this.phaser = phaser;
        }
    }

}
