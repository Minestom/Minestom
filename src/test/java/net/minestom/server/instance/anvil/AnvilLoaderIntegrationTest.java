package net.minestom.server.instance.anvil;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.biome.Biome;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.ValueSources;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.minestom.server.network.NetworkBuffer.SHORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class AnvilLoaderIntegrationTest {
    private static final Path WORLD_RESOURCES = Path.of("src", "test", "resources", "net", "minestom", "server", "instance");

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
                            RegistryKey<Biome> b = chunk.getBiome(x, y, z);
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
    }

    @Test
    public void loadAndSaveChunk(Env env) throws IOException {
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

        instance.saveChunkToStorage(originalChunk);
        instance.unloadChunk(originalChunk);
        assertNull(instance.getChunk(0, 0));

        Chunk reloadedChunk = instance.loadChunk(0, 0).join();
        for (int section = reloadedChunk.getMinSection(); section < reloadedChunk.getMaxSection(); section++) {
            Section originalSection = originalChunk.getSection(section);
            Section reloadedSection = reloadedChunk.getSection(section);

            NetworkBuffer.Type<Palette> biomeSerializer = Palette.biomeSerializer(MinecraftServer.getBiomeRegistry().size());
            // easiest equality check to write is a memory compare on written output
            var original = NetworkBuffer.makeArray(buffer -> {
                buffer.write(SHORT, (short) originalSection.blockPalette().count());
                buffer.write(Palette.BLOCK_SERIALIZER, originalSection.blockPalette());
                buffer.write(biomeSerializer, originalSection.biomePalette());
            });
            var reloaded = NetworkBuffer.makeArray(buffer -> {
                buffer.write(SHORT, (short) reloadedSection.blockPalette().count());
                buffer.write(Palette.BLOCK_SERIALIZER, reloadedSection.blockPalette());
                buffer.write(biomeSerializer, reloadedSection.biomePalette());
            });
            Assertions.assertArrayEquals(original, reloaded);
        }
    }

    @Test
    public void loadAndSaveBlockNBT(Env env) throws IOException {
        var worldFolder = extractWorld("anvil_loader");
        Instance instance = env.createFlatInstance(new AnvilLoader(worldFolder));
        Chunk originalChunk = instance.loadChunk(0, 0).join();

        var nbt = CompoundBinaryTag.builder()
                .putString("hello", "world")
                .build();
        var block = Block.STONE.withNbt(nbt);
        instance.setBlock(BlockVec.ZERO, block);

        instance.saveChunkToStorage(originalChunk).join();
        instance.unloadChunk(originalChunk);
        assertNull(instance.getChunk(0, 0));

        instance.loadChunk(0, 0).join();
        assertEquals(block, instance.getBlock(BlockVec.ZERO));
    }

    private static Collection<BlockVec> provideLocationsForLoadAndSaveBlockHandler() {
        return List.of(BlockVec.ZERO,
                new BlockVec(0, 15, 0),
                new BlockVec(0, 16, 0),
                new BlockVec(0, -15, 0),
                new BlockVec(0, -16, 0),
                new BlockVec(0, 64, 0),
                new BlockVec(15, 0, 15),
                new BlockVec(16, 0, 16),
                new BlockVec(-15, 0, -15),
                new BlockVec(-16, 0, -16)
        );
    }

    @ParameterizedTest
    @MethodSource("provideLocationsForLoadAndSaveBlockHandler")
    public void loadAndSaveBlockHandler(Point point, Env env) throws IOException {
        var worldFolder = extractWorld("anvil_loader");
        Instance instance = env.createFlatInstance(new AnvilLoader(worldFolder));
        Chunk originalChunk = instance.loadChunk(point).join();

        var handler = new BlockHandler() {
            @Override
            public Key getKey() {
                return Key.key("test");
            }
        };
        env.process().block().registerHandler(Block.STONE.key(), () -> handler);

        var nbt = CompoundBinaryTag.builder()
                .putString("hello", "world")
                .build();
        var block = Block.STONE.withNbt(nbt);
        instance.setBlock(point, block);

        instance.saveChunkToStorage(originalChunk).join();
        instance.unloadChunk(originalChunk);
        assertNull(instance.getChunkAt(point));

        instance.loadChunk(point).join();
        assertEquals(block, instance.getBlock(point));
    }

    @Test
    public void loadAndSaveBlockHandlerWithPlacement(Env env) throws IOException {
        final Point point = new BlockVec(100_000, 16, 100_000);
        var worldFolder = extractWorld("anvil_loader");
        Instance instance = env.createFlatInstance(new AnvilLoader(worldFolder));
        Chunk originalChunk = instance.loadChunk(point).join();

        var handler = new BlockHandler() {
            @Override
            public Key getKey() {
                return Block.DIAMOND_BLOCK.key();
            }

            @Override
            public void onPlace(Placement placement) {
                assertEquals(point.x(), placement.getBlockPosition().x());
                assertEquals(point.y(), placement.getBlockPosition().y());
                assertEquals(point.z(), placement.getBlockPosition().z());
            }
        };
        env.process().block().registerHandler(Block.DIAMOND_BLOCK.key(), () -> handler);

        final Block block = Block.DIAMOND_BLOCK.withHandler(handler);
        instance.setBlock(point, block);

        instance.saveChunkToStorage(originalChunk).join();
        instance.unloadChunk(originalChunk);
        assertNull(instance.getChunkAt(point));

        instance.loadChunk(point).join();
    }

    @Test
    public void saveChunks(Env env) throws IOException {
        // load a full vanilla region, not checking any content just making sure it loads without issues.
        var worldFolder = Files.createTempDirectory("minestom-test-world-save-chunks");
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

        for (int chunkX = 0; chunkX < 16; chunkX++) {
            for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
                Chunk chunk = instance.loadChunk(chunkX, chunkZ).join();
                instance.saveChunkToStorage(chunk).join();
                instance.unloadChunk(chunk);
            }
        }
        final AnvilLoader secondChunkLoader = new AnvilLoader(worldFolder) {
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
        final var secondInstance = env.createEmptyInstance(secondChunkLoader);
        for (int chunkX = 0; chunkX < 16; chunkX++) {
            for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
                final Chunk originalChunk = instance.loadChunk(chunkX, chunkZ).join();
                final Chunk chunk = secondInstance.loadChunk(chunkX, chunkZ).join();
                for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                    for (int y = secondInstance.getCachedDimensionType().minY(); y < secondInstance.getCachedDimensionType().maxY(); y++) {
                        for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                            final Block originalBlock = instance.getBlock(x, y, z);
                            final Block block = secondInstance.getBlock(x, y, z);
                            assertEquals(originalBlock, block);
                        }
                    }
                }
            }
        }
    }

    private static Path extractWorld(String resourceName) throws IOException {
        final Path worldFolder = Files.createTempDirectory("minestom-test-world-" + resourceName);

        // https://stackoverflow.com/a/60621544
        Files.walkFileTree(WORLD_RESOURCES.resolve(resourceName), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(worldFolder.resolve(WORLD_RESOURCES.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, worldFolder.resolve(WORLD_RESOURCES.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
        return worldFolder.resolve(resourceName);
    }
}
