package net.minestom.server.thread;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown when an acquirable element is accessed without proper ownership.
 */
public final class AcquirableOwnershipException extends RuntimeException {
    private final Thread currentThread;
    private final Thread initThread;
    private final Thread assignedThread;
    private final Object value;

    @ApiStatus.Internal
    public AcquirableOwnershipException(@NotNull Thread currentThread,
                                        @NotNull Thread initThread,
                                        @Nullable Thread assignedThread,
                                        @NotNull Object value) {
        super(buildMessage(currentThread, initThread, assignedThread, value));
        this.currentThread = currentThread;
        this.initThread = initThread;
        this.assignedThread = assignedThread;
        this.value = value;
    }

    private static String buildMessage(@NotNull Thread currentThread,
                                       @NotNull Thread initThread,
                                       @Nullable Thread assignedThread,
                                       @NotNull Object value) {
        final String valueString = value.toString();
        if (assignedThread != null) {
            return """
                    Thread ownership assertion failed for %s:
                      Current thread:  %s
                      Assigned thread: %s
                      Problem: The element is assigned to a different thread and not currently owned.
                      Solution: Use sync() or lock() to acquire ownership before accessing the element.
                    """.formatted(
                    valueString,
                    currentThread.getName(),
                    assignedThread.getName()
            );
        } else {
            return """
                    Thread ownership assertion failed for %s:
                      Current thread:        %s
                      Initialization thread: %s
                      Problem: The element is not yet initialized and is being accessed from a different thread.
                      Solution: Handle the element in the same thread it has been initialized in until it is fully initialized.
                    """.formatted(
                    valueString,
                    currentThread.getName(),
                    initThread.getName()
            );
        }
    }

    /**
     * The current thread that attempted to access the acquirable element.
     */
    public @NotNull Thread currentThread() {
        return currentThread;
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
     * The value of the acquirable element that caused the ownership failure.
     */
    public @NotNull Object value() {
        return value;
    }
}
