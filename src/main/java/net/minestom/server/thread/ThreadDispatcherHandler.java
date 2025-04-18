package net.minestom.server.thread;

import net.minestom.server.Tickable;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public abstract class ThreadDispatcherHandler<P extends Tickable> {

    protected ThreadDispatcher<P> dispatcher;

    public void setupDispatcher(ThreadDispatcher<P> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public abstract @NotNull ThreadProvider<P> provider();
    public abstract void onEntityRefreshCurrentChunk(@NotNull Entity entity, @NotNull Chunk currentChunk);
    public abstract void onEntityRemoval(@NotNull Entity entity);
    public abstract void onEntitySetInstance(@NotNull Entity entity, @NotNull Instance instance);
    public abstract void onChunkUnload(@NotNull Chunk chunk);
    public abstract void onChunkCached(@NotNull Chunk chunk, @NotNull Instance instance);
    public abstract void onInstanceUnregister(@NotNull Instance instance);
    public abstract void onInstanceRegister(@NotNull Instance instance);

}
