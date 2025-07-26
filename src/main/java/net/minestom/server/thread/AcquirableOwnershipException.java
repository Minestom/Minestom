package net.minestom.server.thread;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown when an acquirable element is accessed without proper ownership.
 */
public final class AcquirableOwnershipException extends RuntimeException {
    private final Thread initThread, assignedThread;
    private final Object element;

    @ApiStatus.Internal
    public AcquirableOwnershipException(@NotNull Thread initThread, @Nullable Thread assignedThread,
                                        @NotNull Object element) {
        super(buildMessage(initThread, assignedThread, element));
        this.initThread = initThread;
        this.assignedThread = assignedThread;
        this.element = element;
    }

    private static String buildMessage(@NotNull Thread initThread, @Nullable Thread assignedThread,
                                       @NotNull Object value) {
        final String valueString = value.toString();
        if (assignedThread != null) {
            return """
                    Thread ownership assertion failed for %s:
                      Current thread:  %s
                      Assigned thread: %s
                      Problem: The element is assigned to a different thread and is not currently owned.
                      Solution: Use Acquirable#sync() or Acquirable#lock() to acquire ownership before accessing the element.
                    """.formatted(valueString,
                    Thread.currentThread().getName(),
                    assignedThread.getName()
            );
        } else {
            return """
                    Thread ownership assertion failed for %s:
                      Current thread:        %s
                      Initialization thread: %s
                      Problem: The element is not yet initialized and is being accessed from a different thread.
                      Solution: Handle the element in the same thread it has been initialized in until it is fully initialized.
                    """.formatted(valueString,
                    Thread.currentThread().getName(),
                    initThread.getName()
            );
        }
    }

    /**
     * The thread that initialized the acquirable element.
     */
    public @NotNull Thread initThread() {
        return initThread;
    }

    /**
     * The thread to which the acquirable element is assigned.
     * May be null if the element is not yet initialized.
     */
    public @Nullable Thread assignedThread() {
        return assignedThread;
    }

    /**
     * The acquirable element that caused the ownership failure.
     */
    public @NotNull Object element() {
        return element;
    }
}
