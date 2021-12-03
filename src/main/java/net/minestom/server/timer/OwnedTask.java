package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

public sealed interface OwnedTask permits OwnedTaskImpl {
    int id();

    @NotNull ExecutionType executionType();

    @NotNull Scheduler owner();

    /**
     * Unpark the tasks to be executed during next processing.
     *
     * @throws IllegalStateException if the task is not parked
     */
    void unpark();

    void stop();

    boolean isAlive();
}
