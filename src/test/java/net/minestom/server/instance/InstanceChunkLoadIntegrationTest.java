package net.minestom.server.instance;


import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;

@EnvTest
public class InstanceChunkLoadIntegrationTest {

    @Test
    public void testSyncChunkLoad(Env env) {
        // Kind of a weird test because the test method is orchestrating ticks, meaning that the test thread is the tick thread.

        var expectedThread = new AtomicReference<Thread>(null);
        var instance = env.createFlatInstance(new NoopLoader(false));
        instance.loadChunk(0, 0).thenRun(() -> {
            // Will run in the thread which completed the future, which should always be the instance tick thread
            expectedThread.set(Thread.currentThread());
        });
        env.tick(); // Tick once to run the scheduled task

        assertSame(Thread.currentThread(), expectedThread.get(), "expected the callback to be executed in the same thread as the tick");
    }

    @Test
    public void testAsyncChunkLoad(Env env) {
        // Kind of a weird test because the test method is orchestrating ticks, meaning that the test thread is the tick thread.

        var expectedThread = new AtomicReference<Thread>(null);
        var instance = env.createFlatInstance(new NoopLoader(true));
        instance.loadChunk(0, 0).thenRun(() -> {
            // Will run in the thread which completed the future, which should always be the instance tick thread
            expectedThread.set(Thread.currentThread());
        });
        env.tickWhile(() -> expectedThread.get() == null, Duration.ofSeconds(5)); // Tick once to run the scheduled task

        assertSame(Thread.currentThread(), expectedThread.get(), "expected the callback to be executed in the same thread as the tick");
    }

    private static class NoopLoader implements IChunkLoader {
        private final boolean isParallel;

        private NoopLoader(boolean isParallel) {
            this.isParallel = isParallel;
        }

        @Override
        public boolean supportsParallelLoading() {
            return isParallel;
        }

        @Override
        public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
            return CompletableFuture.completedFuture(instance.getChunkSupplier().createChunk(instance, chunkX, chunkZ));
        }

        @Override
        public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
