package net.minestom.server.entity.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChunkLoadEvent;
import net.minestom.server.event.player.PlayerChunkUnloadEvent;
import net.minestom.server.network.packet.client.play.ClientStatusPacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UnloadChunkPacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@EnvTest
public class PlayerRespawnChunkTest {


    @Test
    public void testChunkUnloadsOnRespawn(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        Player player = connection.connect(instance, new Pos(0, 40, 0)).join();
        player.teleport(new Pos(32, 40, 32)).join();

        var unloadChunkTracker = connection.trackIncoming(UnloadChunkPacket.class);
        player.setHealth(0);
        player.respawn();
        // Since client unloads the chunks, we shouldn't receive any unload packets
        unloadChunkTracker.assertCount(0);
    }

    @Test
    public void testChunkReloadCount(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        Player player = connection.connect(instance, new Pos(0, 40, 0)).join();

        var loadChunkTracker = connection.trackIncoming(ChunkDataPacket.class);
        player.setHealth(0);
        player.respawn();
        // Player should have all their chunks reloaded
        int chunkLoads = ChunkUtils.getChunkCount(MinecraftServer.getChunkViewDistance());
        loadChunkTracker.assertCount(chunkLoads);
    }

    @Test
    public void testPlayerTryRespawn(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        Player player = connection.connect(instance, new Pos(0, 40, 0)).join();

        player.eventNode().addListener(PlayerChunkLoadEvent.class, playerChunkLoadEvent -> {
            // We should only load chunks around the spawnpoint
            assertTrue(Math.abs(playerChunkLoadEvent.getChunkX()) <= MinecraftServer.getChunkViewDistance() && Math.abs(playerChunkLoadEvent.getChunkZ()) <= MinecraftServer.getChunkViewDistance());
        });
        player.setHealth(0);
        player.addPacketToQueue(new ClientStatusPacket(ClientStatusPacket.Action.PERFORM_RESPAWN));
        player.interpretPacketQueue();
    }

    @Test
    public void testPlayerUnloadChunkEventOnRespawn(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        Player player = connection.connect(instance, new Pos(0, 40, 0)).join();
        player.setInstance(instance).join(); // TODO: REMOVE


        // Get a list from -8 to 8, since we will be unloading
        ArrayList<Integer> chunkZList = new ArrayList<>();
        for(int i = -MinecraftServer.getChunkViewDistance(); i <= MinecraftServer.getChunkViewDistance(); i++) {
            chunkZList.add(i);
        }
        ArrayList<Integer> chunkZListCopy = new ArrayList<>(chunkZList);

        var listener = env.listen(PlayerChunkUnloadEvent.class);
        // We will now trigger a bunch of chunk unloads from moving - ensure they are done properly
        listener.followup(event -> {
            assertEquals(-MinecraftServer.getChunkViewDistance(), event.getChunkX());
            assertTrue(chunkZList.contains(event.getChunkZ()));
            chunkZList.remove(Integer.valueOf(event.getChunkZ()));
        });
        // Trigger chunk unloading from movement
        player.teleport(new Pos(16, 40, 0)).join();
        assertTrue(chunkZList.isEmpty());
        // We will now trigger another round of chunk unloads because we have shifted positions from the respawn
        listener.followup(event -> {
            assertEquals(MinecraftServer.getChunkViewDistance() + 1, event.getChunkX());
            assertTrue(chunkZListCopy.contains(event.getChunkZ()));
            chunkZListCopy.remove(Integer.valueOf(event.getChunkZ()));
        });
        player.setHealth(0);
        player.addPacketToQueue(new ClientStatusPacket(ClientStatusPacket.Action.PERFORM_RESPAWN));
        player.interpretPacketQueue();
        assertTrue(chunkZListCopy.isEmpty());
    }
}
