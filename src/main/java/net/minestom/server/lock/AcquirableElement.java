package net.minestom.server.lock;

import net.minestom.server.thread.BatchThread;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.UUID;
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
                acquisitionLock.getPhaser().arriveAndDeregister();
            }
        }
    }

    @NotNull
    T unsafeUnwrap();

    @NotNull
    Handler getHandler();

    class Handler {

        public static void processQueue(@NotNull Queue<AcquisitionLock> acquisitionQueue) {
            //System.out.println("PROCESS QUEUE " + acquisitionQueue.hashCode());
            if (!acquisitionQueue.isEmpty()) {
                AcquisitionLock lock;
                synchronized (AcquirableElement.class) {
                    Phaser phaser = new Phaser(1);
                    long nano = System.nanoTime();
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
                    //System.out.println("total time "+(System.nanoTime()-nano));
                }
            }
        }

        private volatile UUID periodIdentifier = null;
        private volatile Queue<AcquisitionLock> acquisitionQueue = null;

        public boolean tryAcquisition(@NotNull AcquisitionLock lock) {
            final Thread currentThread = Thread.currentThread();
            final boolean isBatchThread = currentThread instanceof BatchThread;
            final UUID threadIdentifier = isBatchThread ?
                    ((BatchThread) currentThread).getIdentifier() : null;
            final boolean differentThread = threadIdentifier == null ||
                    !threadIdentifier.equals(periodIdentifier);

            if (differentThread && periodIdentifier != null && acquisitionQueue != null) {

                //System.out.println("diff " + periodIdentifier + " " + threadIdentifier);
                try {

                    synchronized (lock) {

                        synchronized (AcquirableElement.class) {
                            if (isBatchThread) {
                                processQueue(((BatchThread) currentThread).getWaitingAcquisitionQueue());
                            }
                            processQueue(acquisitionQueue);

                            acquisitionQueue.add(lock);
                            /*System.out.println("ADD WAIT " + currentThread.getName() + " " +
                                    acquisitionQueue.hashCode() + " " + acquisitionQueue.size() + " " +
                                    ((BatchThread) currentThread).getWaitingAcquisitionQueue().hashCode() + " " +
                                    ((BatchThread) currentThread).getWaitingAcquisitionQueue().size());*/
                        }

                        lock.wait();
                    }
                    return false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }

            } else {
                //System.out.println("GOOD");
                return true;
            }
        }

        public void refreshThread(@NotNull UUID identifier, @NotNull Queue<AcquisitionLock> acquisitionQueue) {
            this.periodIdentifier = identifier;
            this.acquisitionQueue = acquisitionQueue;
        }

        public void acquisitionTick() {
            processQueue(acquisitionQueue);
        }

        @NotNull
        public UUID getPeriodIdentifier() {
            return periodIdentifier;
        }
    }

    class AcquisitionLock {

        private volatile Phaser phaser;

        public Phaser getPhaser() {
            return phaser;
        }

        public void setPhaser(Phaser phaser) {
            this.phaser = phaser;
        }
    }

}
