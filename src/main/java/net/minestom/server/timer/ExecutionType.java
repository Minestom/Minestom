package net.minestom.server.timer;

public enum ExecutionType {
    /**
     * Schedule tasks to execute at the beginning of the {@link Schedulable} tick
     */
    TICK_START,
    /**
     * Schedule tasks to execute at the end of the {@link Schedulable} tick
     */
    TICK_END,
    /**
     * @deprecated use {@link ExecutionType#TICK_START}
     * to be removed in 1.20.5
     */
    @Deprecated()
    SYNC,
    /**
     * to be removed in 1.20.5
     */
    @Deprecated()
    ASYNC
}
