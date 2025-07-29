package net.minestom.server.instance.loading;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class AnvilLoaderLoadBenchmark {

    private static final Path WORLD_RESOURCES = Path.of("src", "jmh", "resources", "net", "minestom", "server", "instance");

    @Setup
    public void setup() {
        MinecraftServer.updateProcess();
        for (final Block block : MinecraftServer.process().blocks().values()) {
            if (block.isAir()) {
                continue;
            }
            MinecraftServer.getBlockManager().registerHandler(block.key(), () -> block::key);
        }
    }

    @Benchmark
    public void chunkLoading() throws IOException {
        final var worldFolder = extractWorld("anvil_vanilla_sample");
        final AnvilLoader chunkLoader = new AnvilLoader(worldFolder) {
            // Force loads inside current thread
            @Override
            public boolean supportsParallelLoading() {
                return false;
            }

            @Override
            public boolean supportsParallelSaving() {
                return false;
            }
        };
        final Instance instance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD, chunkLoader);
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                instance.loadChunk(0, 0).join();
            }
        }
    }


    private static Path extractWorld(@NotNull String resourceName) throws IOException {
        final Path worldFolder = Files.createTempDirectory("minestom-test-world-" + resourceName);

        // https://stackoverflow.com/a/60621544
        Files.walkFileTree(WORLD_RESOURCES.resolve(resourceName), new SimpleFileVisitor<>() {
            @Override
            public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(worldFolder.resolve(WORLD_RESOURCES.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, worldFolder.resolve(WORLD_RESOURCES.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
        return worldFolder.resolve(resourceName);
    }

}
