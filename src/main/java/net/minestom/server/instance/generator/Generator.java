package net.minestom.server.instance.generator;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.thread.MinestomThreadPool;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@FunctionalInterface
public interface Generator {
    ExecutorService WORLD_GEN_POOL = new MinestomThreadPool(MinecraftServer.THREAD_COUNT_WORLD_GEN, MinecraftServer.THREAD_NAME_WORLD_GEN);

    List<CompletableFuture<SectionResult>> generate(Instance instance, GenerationRequest request);
}
