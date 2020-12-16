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
        getHandler().tryAcquisition(new Object());
        consumer.accept(unsafeUnwrap());
    }

    @NotNull
    T unsafeUnwrap();

    Handler getHandler();

    class Handler {

        private static final ThreadLocal<UUID> EXECUTION_IDENTIFIER = ThreadLocal.withInitial(() -> UUID.randomUUID());

        public static void resetIdentifier() {
            //EXECUTION_IDENTIFIER.remove();
        }

        private final Queue<Object> toNotifyQueue = Queues.newConcurrentLinkedQueue();

        private volatile UUID periodIdentifier = UUID.randomUUID();

        public void tryAcquisition(Object test) {
            System.out.println("test "+periodIdentifier);
            if (!periodIdentifier.equals(EXECUTION_IDENTIFIER.get())) {
                System.out.println("diff " + periodIdentifier + " " + EXECUTION_IDENTIFIER.get());
                try {
                    this.toNotifyQueue.add(test);
                    System.out.println("ADD WAIT " + Thread.currentThread().getName());
                    synchronized (test) {
                        test.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("GOOD");
            }
        }

        public void startTick() {
            this.periodIdentifier = EXECUTION_IDENTIFIER.get();
        }

        public void endTick() {
            System.out.println("END TICK");
            Object object;
            while ((object = toNotifyQueue.poll()) != null) {
                System.out.println("NOTIFY");
                synchronized (object) {
                    System.out.println("end modify");
                    object.notifyAll();
                }
            }
        }

        public UUID getPeriodIdentifier() {
            return periodIdentifier;
        }
    }
}
