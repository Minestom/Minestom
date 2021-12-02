package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

public interface Schedulable {
    @NotNull Scheduler scheduler();
}
