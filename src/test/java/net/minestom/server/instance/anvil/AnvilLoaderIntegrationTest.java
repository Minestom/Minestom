package net.minestom.server.instance.anvil;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.biome.Biome;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.minestom.server.network.NetworkBuffer.SHORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class AnvilLoaderIntegrationTest {

    private static final Path testRoot = Path.of("src", "test", "resources", "net", "minestom", "server", "instance");

    @Test
    public void loadVanillaRegion(Env env) throws IOException {
        // load a full vanilla region, not checking any content just making sure it loads without issues.

        var worldFolder = extractWorld("anvil_vanilla_sample");
        AnvilLoader chunkLoader = new AnvilLoader(worldFolder) {
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
        Instance instance = env.createFlatInstance(chunkLoader);

        for (int chunkX = 0; chunkX < 32; chunkX++) {
            for (int chunkZ = 0; chunkZ < 32; chunkZ++) {
                Chunk chunk = instance.loadChunk(chunkX, chunkZ).join();
                instance.unloadChunk(chunk);
            }
        }
    }

    @Test
    public void parallelSaveNonexistentFiles(Env env) throws Exception {
        var worldFolder = Files.createTempDirectory("minestom-test-world-parallel-save");
        AnvilLoader chunkLoader = new AnvilLoader(worldFolder);
        Instance instance = env.createFlatInstance(chunkLoader);

        for (int chunkX = 0; chunkX < 32; chunkX++) {
            for (int chunkZ = 0; chunkZ < 32; chunkZ++) {
                instance.loadChunk(chunkX, chunkZ).join();
            }
        }

        AtomicReference<Throwable> exception = new AtomicReference<>();
        env.process().exception().setExceptionHandler((throwable) -> {
            exception.set(throwable);
            throwable.printStackTrace();
        });
        instance.saveChunksToStorage().join();
        assertNull(exception.get());
    }

    @Test
    public void loadHouse(Env env) throws IOException {
        // load a world that contains only a basic house and make sure it is loaded properly

        var worldFolder = extractWorld("anvil_loader");
        AnvilLoader chunkLoader = new AnvilLoader(worldFolder) {
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
        Instance instance = env.createFlatInstance(chunkLoader);

        Consumer<Chunk> checkChunk = chunk -> {
            synchronized (chunk) {
                assertEquals(-4, chunk.getMinSection());
                assertEquals(20, chunk.getMaxSection());

                for (int y = 0; y < 16; y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            DynamicRegistry.Key<Biome> b = chunk.getBiome(x, y, z);
                            assertEquals(Biome.PLAINS, b);
                        }
                    }
                }
            }
        };

        for (int x = -2; x < 2; x++) {
            for (int z = -2; z < 2; z++) {
                checkChunk.accept(instance.loadChunk(x, z).join()); // this is a test so we don't care too much about waiting for each chunk
            }
        }

        // wooden house with nylium ground. Open world inside MC to check out

        // center of world
        assertEquals(Block.BEDROCK, instance.getBlock(0, 0, 0));
        // nylium stripes in front and back of house
        for (int z = -4; z <= 0; z++) {
            assertEquals(Block.WARPED_NYLIUM, instance.getBlock(4, 0, z));
            assertEquals(Block.WARPED_NYLIUM, instance.getBlock(-3, 0, z));
            assertEquals(Block.WARPED_NYLIUM, instance.getBlock(-4, 0, z));
        }

        // side walls
        for (int x = -2; x <= 3; x++) {
            if (x != 0) { // bedrock block at center
                assertEquals(Block.NETHERRACK, instance.getBlock(x, 0, 0));
            }
            assertEquals(Block.NETHERRACK, instance.getBlock(x, 0, -4));

            assertEquals(Block.OAK_PLANKS, instance.getBlock(x, 1, 0));
            assertEquals(Block.OAK_PLANKS, instance.getBlock(x, 1, -4));
            assertEquals(Block.OAK_PLANKS, instance.getBlock(x, 2, 0));
            assertEquals(Block.OAK_PLANKS, instance.getBlock(x, 2, -4));
        }

        // back wall
        for (int z = -4; z <= 0; z++) {
            assertEquals(Block.NETHERRACK, instance.getBlock(-2, 0, z));

            assertEquals(Block.OAK_PLANKS, instance.getBlock(-2, 1, z));
            assertEquals(Block.OAK_PLANKS, instance.getBlock(-2, 2, z));
        }

        // door
        Block baseDoor = Block.ACACIA_DOOR
                .withProperty("facing", "west")
                .withProperty("hinge", "left")
                .withProperty("open", "false")
                .withProperty("powered", "false");
        Block bottomDoorPart = baseDoor.withProperty("half", "lower");
        Block topDoorPart = baseDoor.withProperty("half", "upper");
        assertEquals(bottomDoorPart, instance.getBlock(3, 1, -3));
        assertEquals(topDoorPart, instance.getBlock(3, 2, -3));

        // light blocks
        Block endRod = Block.END_ROD.withProperty("facing", "up");
        assertEquals(endRod, instance.getBlock(-1, 1, -1));
        assertEquals(Block.TORCH, instance.getBlock(-1, 2, -1));

        // flower pot
        assertEquals(Block.OAK_PLANKS, instance.getBlock(-1, 1, -3));
        assertEquals(Block.POTTED_POPPY, instance.getBlock(-1, 2, -3));

        env.destroyInstance(instance);
    }

    @Test
    public void loadAndSaveChunk(Env env) throws IOException, InterruptedException {
        var worldFolder = extractWorld("anvil_loader");
        Instance instance = env.createFlatInstance(new AnvilLoader(worldFolder) {
            // Force loads inside current thread
            @Override
            public boolean supportsParallelLoading() {
                return false;
            }

            @Override
            public boolean supportsParallelSaving() {
                return false;
            }
        });
        Chunk originalChunk = instance.loadChunk(0, 0).join();

        synchronized (originalChunk) {
            instance.saveChunkToStorage(originalChunk);
            instance.unloadChunk(originalChunk);
            while (originalChunk.isLoaded()) {
                Thread.sleep(1);
            }
        }

        Chunk reloadedChunk = instance.loadChunk(0, 0).join();
        for (int section = reloadedChunk.getMinSection(); section < reloadedChunk.getMaxSection(); section++) {
            Section originalSection = originalChunk.getSection(section);
            Section reloadedSection = reloadedChunk.getSection(section);

            // easiest equality check to write is a memory compare on written output
            var original = NetworkBuffer.makeArray(buffer -> {
                buffer.write(SHORT, (short) originalSection.blockPalette().count());
                buffer.write(Palette.BLOCK_SERIALIZER, originalSection.blockPalette());
                buffer.write(Palette.BIOME_SERIALIZER, originalSection.biomePalette());
            });
            var reloaded = NetworkBuffer.makeArray(buffer -> {
                buffer.write(SHORT, (short) reloadedSection.blockPalette().count());
                buffer.write(Palette.BLOCK_SERIALIZER, reloadedSection.blockPalette());
                buffer.write(Palette.BIOME_SERIALIZER, reloadedSection.biomePalette());
            });
            Assertions.assertArrayEquals(original, reloaded);
        }

        env.destroyInstance(instance);
    }

    private static Path extractWorld(@NotNull String resourceName) throws IOException {
        var worldFolder = Files.createTempDirectory("minestom-test-world-" + resourceName);

        // https://stackoverflow.com/a/60621544
        Files.walkFileTree(testRoot.resolve(resourceName), new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(worldFolder.resolve(testRoot.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, worldFolder.resolve(testRoot.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });

        return worldFolder.resolve(resourceName);
    }
}
