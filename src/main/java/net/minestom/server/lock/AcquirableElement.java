package net.minestom.server.lock;

import com.google.common.collect.Queues;
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
    default void acquire(Consumer<T> consumer) {
        boolean sameThread = getHandler().tryAcquisition(new Object());
        final T unwrap = unsafeUnwrap();
        synchronized (unwrap) {
            consumer.accept(unwrap);
        }
    }

    @NotNull
    T unsafeUnwrap();

    Handler getHandler();

    class Handler {

        private static final ThreadLocal<Queue<Object>> QUEUE_IDENTIFIER = ThreadLocal.withInitial(Queues::newConcurrentLinkedQueue);
        private static final ThreadLocal<UUID> EXECUTION_IDENTIFIER = ThreadLocal.withInitial(UUID::randomUUID);

        public static void reset() {
            processQueue();

            QUEUE_IDENTIFIER.remove();
            EXECUTION_IDENTIFIER.remove();
        }

        public static void processQueue() {
            final Queue<Object> queue = QUEUE_IDENTIFIER.get();
            //System.out.println("PROCESS QUEUE");
            Object object;
            while ((object = queue.poll()) != null) {
                System.out.println("NOTIFY");
                synchronized (object) {
                    System.out.println("end modify");
                    object.notify();
                }
            }
            // TODO wait until all the queue consumers are executed
        }

        private volatile UUID periodIdentifier = UUID.randomUUID();

        public boolean tryAcquisition(Object test) {
            System.out.println("test " + periodIdentifier);
            if (!periodIdentifier.equals(EXECUTION_IDENTIFIER.get())) {
                System.out.println("diff " + periodIdentifier + " " + EXECUTION_IDENTIFIER.get());
                try {
                    QUEUE_IDENTIFIER.get().add(test);
                    System.out.println("ADD WAIT " + Thread.currentThread().getName());
                    synchronized (test) {
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

        public void startTick() {
            this.periodIdentifier = EXECUTION_IDENTIFIER.get();
        }

        public void endTick() {
            processQueue();
        }

        public UUID getPeriodIdentifier() {
            return periodIdentifier;
        }
    }
}
