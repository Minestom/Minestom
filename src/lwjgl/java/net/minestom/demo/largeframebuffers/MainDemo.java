package net.minestom.demo.largeframebuffers;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.world.*;
import net.minestom.server.world.batch.ChunkBatch;
import net.minestom.server.block.Block;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.Position;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MainDemo {

    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        WorldManager worldManager = MinecraftServer.getWorldManager();
        // Create the world
        WorldContainer worldContainer = worldManager.createWorldContainer();
        // Set the ChunkGenerator
        worldContainer.setChunkGenerator(new GeneratorDemo());
        // Enable the auto chunk loading (when players come close)
        worldContainer.enableAutoChunkLoad(true);

        // Add event listeners
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
        connectionManager.addPlayerInitialization(player -> {
            // Set the spawning world
            player.addEventCallback(PlayerLoginEvent.class, event -> {
                event.setSpawningWorld(worldContainer);
                player.setRespawnPoint(new Position(0, 45, 0));
            });

            // Teleport the player at spawn
            player.addEventCallback(PlayerSpawnEvent.class, event -> {
                player.teleport(new Position(0, 45, 0));
                player.setGameMode(GameMode.CREATIVE);
            });
        });

        // Start the server
        minecraftServer.start("localhost", 25565);
    }

    private static class GeneratorDemo implements ChunkGenerator {

        @Override
        public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
            // Set chunk blocks
            for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++)
                for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    for (byte y = 0; y < 40; y++) {
                        batch.setBlock(x, y, z, Block.STONE);
                    }
                }
        }

        @Override
        public void fillBiomes(@NotNull Biome[] biomes, int chunkX, int chunkZ) {
            Arrays.fill(biomes, MinecraftServer.getBiomeManager().getById(0));
        }

        @Override
        public List<ChunkPopulator> getPopulators() {
            return null;
        }
    }

}
