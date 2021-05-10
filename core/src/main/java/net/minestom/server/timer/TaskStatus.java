package net.minestom.server.timer;

/**
 * An enumeration that representing all available statuses for a {@link Task}
 */
public enum TaskStatus {

    /**
     * The task is execution and is currently running
     */
    SCHEDULED,
    /**
     * The task was cancelled with {@link Task#cancel()}
     */
    CANCELLED,
    /**
     * The task has been completed. This only applies to tasks without repetition
     */
    FINISHED,

}
