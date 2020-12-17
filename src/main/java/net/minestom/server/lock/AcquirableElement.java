package net.minestom.server.lock;

import net.minestom.server.thread.BatchThread;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.UUID;
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
        boolean sameThread = getHandler().tryAcquisition(new Object());
        final T unwrap = unsafeUnwrap();
        if (sameThread) {
            consumer.accept(unwrap);
        } else {
            synchronized (unwrap) {
                consumer.accept(unwrap);
            }
        }
    }

    @NotNull
    T unsafeUnwrap();

    @NotNull
    Handler getHandler();

    class Handler {

        public static void processQueue(@NotNull Queue<Object> acquisitionQueue) {
            //System.out.println("PROCESS QUEUE " + acquisitionQueue.hashCode());
            Object object;
            while ((object = acquisitionQueue.poll()) != null) {
                //System.out.println("NOTIFY " + acquisitionQueue.hashCode());
                synchronized (object) {
                    //System.out.println("end modify");
                    object.notify();
                }
            }
            // TODO wait until all the queue consumers are executed
        }

        private volatile UUID periodIdentifier = null;
        private volatile Queue<Object> acquisitionQueue = null;

        public boolean tryAcquisition(@NotNull Object test) {
            final Thread currentThread = Thread.currentThread();
            final boolean isBatchThread = currentThread instanceof BatchThread;
            final UUID threadIdentifier = isBatchThread ?
                    ((BatchThread) currentThread).getIdentifier() : null;
            final boolean differentThread = threadIdentifier == null ||
                    !threadIdentifier.equals(periodIdentifier);

            if (differentThread && periodIdentifier != null && acquisitionQueue != null) {

                System.out.println("diff " + periodIdentifier + " " + threadIdentifier);
                try {

                    if (isBatchThread) {
                        processQueue(((BatchThread) currentThread).getWaitingAcquisitionQueue());
                    }

                    synchronized (test) {

                        acquisitionQueue.add(test);
                        /*System.out.println("ADD WAIT " + currentThread.getName() + " " +
                                acquisitionQueue.hashCode() + " " + acquisitionQueue.size() + " " +
                                ((BatchThread) currentThread).getWaitingAcquisitionQueue().hashCode() + " " +
                                ((BatchThread) currentThread).getWaitingAcquisitionQueue().size());*/
                        test.wait();
                    }
                    return false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }

            } else {
                System.out.println("GOOD");
                return true;
            }
        }

        public void refreshThread(@NotNull UUID identifier, @NotNull Queue<Object> acquisitionQueue) {
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
}
