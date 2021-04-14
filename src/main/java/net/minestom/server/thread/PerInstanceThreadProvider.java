package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class PerInstanceThreadProvider extends ThreadProvider {

    public PerInstanceThreadProvider(int threadCount) {
        super(threadCount);
    }

    @Override
    public int findThread(@NotNull Chunk chunk) {
        return ThreadLocalRandom.current().nextInt(getThreads().size());
    }
}
