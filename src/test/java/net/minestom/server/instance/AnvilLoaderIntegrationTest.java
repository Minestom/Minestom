package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.world.biomes.Biome;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class AnvilLoaderIntegrationTest {

    private static final Path testRoot = Path.of("src", "test", "resources", "net", "minestom", "server", "instance", "anvil_loader");
    private static final Path worldFolder = Path.of("integration_test_world");


    @BeforeAll
    public static void prepareTest() throws IOException {
        // https://stackoverflow.com/a/60621544
        Files.walkFileTree(testRoot, new SimpleFileVisitor<>() {

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
    }



    @Test
    public void loadHouse(Env env) {
        // load a world that contains only a basic house and make sure it is loaded properly

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

                // TODO: skylight
                // TODO: block light
                for (int y = 0; y < 16; y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            Biome b = chunk.getBiome(x, y, z);
                            assertEquals(NamespaceID.from("minecraft:plains"), b.name());
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
            if(x != 0) { // bedrock block at center
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
                .withProperty("powered", "false")
                ;
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
    public void loadAndSaveChunk(Env env) throws InterruptedException {
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
        Chunk originalChunk = instance.loadChunk(0,0).join();

        synchronized (originalChunk) {
            instance.saveChunkToStorage(originalChunk);
            instance.unloadChunk(originalChunk);
            while(originalChunk.isLoaded()) {
                Thread.sleep(1);
            }
        }

        Chunk reloadedChunk = instance.loadChunk(0,0).join();
        for(int section = reloadedChunk.getMinSection(); section < reloadedChunk.getMaxSection(); section++) {
            Section originalSection = originalChunk.getSection(section);
            Section reloadedSection = reloadedChunk.getSection(section);

            // easiest equality check to write is a memory compare on written output
            BinaryWriter originalWriter = new BinaryWriter();
            BinaryWriter reloadedWriter = new BinaryWriter();
            originalSection.write(originalWriter);
            reloadedSection.write(reloadedWriter);

            Assertions.assertArrayEquals(originalWriter.toByteArray(), reloadedWriter.toByteArray());
        }

        env.destroyInstance(instance);
    }

    @AfterAll
    public static void cleanupTest() throws IOException {
        Files.walkFileTree(worldFolder, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
