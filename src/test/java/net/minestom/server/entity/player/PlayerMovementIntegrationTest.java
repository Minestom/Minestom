package net.minestom.server.entity.player;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.packet.client.common.ClientSettingsPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientTeleportConfirmPacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.EntityPositionPacket;
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket;
import net.minestom.server.network.packet.server.play.UnloadChunkPacket;
import net.minestom.server.network.player.ClientSettings;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.testing.TestConnection;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class PlayerMovementIntegrationTest {

    @Test
    public void teleportConfirm(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 40, 0));
        // No confirmation
        p1.addPacketToQueue(new ClientPlayerPositionPacket(new Pos(0.2, 40, 0), true, false));
        p1.interpretPacketQueue();
        assertEquals(new Pos(0, 40, 0), p1.getPosition());
        // Confirmation
        p1.addPacketToQueue(new ClientTeleportConfirmPacket(p1.getLastSentTeleportId()));
        p1.addPacketToQueue(new ClientPlayerPositionPacket(new Pos(0.2, 40, 0), true, false));
        p1.interpretPacketQueue();
        assertEquals(new Pos(0.2, 40, 0), p1.getPosition());
    }

    // FIXME
    //@Test
    public void singleTickMovementUpdate(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var p1 = env.createPlayer(instance, new Pos(0, 40, 0));
        connection.connect(instance, new Pos(0, 40, 0));

        p1.addPacketToQueue(new ClientTeleportConfirmPacket(p1.getLastSentTeleportId()));
        p1.addPacketToQueue(new ClientPlayerPositionPacket(new Pos(0.2, 40, 0), true, false));
        p1.addPacketToQueue(new ClientPlayerPositionPacket(new Pos(0.4, 40, 0), true, false));
        var tracker = connection.trackIncoming(EntityPositionPacket.class);
        p1.interpretPacketQueue();

        // Position update should only be sent once per tick independently of the number of packets
        tracker.assertSingle();
    }

    @Test
    public void chunkUpdateDebounceTest(Env env) {
        final Instance flatInstance = env.createFlatInstance();
        final int viewDiameter = (ServerFlag.CHUNK_VIEW_DISTANCE + 1) * 2 + 1;
        // Preload all possible chunks to avoid issues due to async loading
        Set<CompletableFuture<Chunk>> chunks = new HashSet<>();
        ChunkRange.chunksInRange(0, 0, viewDiameter + 2, (x, z) -> chunks.add(flatInstance.loadChunk(x, z)));
        CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
        final TestConnection connection = env.createConnection();
        Collector<ChunkDataPacket> chunkDataPacketCollector = connection.trackIncoming(ChunkDataPacket.class);
        final Player player = connection.connect(flatInstance, new Pos(0.5, 40, 0.5));
        // Initial join
        chunkDataPacketCollector.assertCount(ChunkRange.chunksCount(player.effectiveViewDistance()));
        player.addPacketToQueue(new ClientTeleportConfirmPacket(player.getLastSentTeleportId()));

        // Move to next chunk
        chunkDataPacketCollector = connection.trackIncoming(ChunkDataPacket.class);
        player.addPacketToQueue(new ClientPlayerPositionPacket(new Vec(-0.5, 40, 0.5), true, false));
        player.interpretPacketQueue();
        chunkDataPacketCollector.assertCount(viewDiameter);

        // Move to next chunk
        chunkDataPacketCollector = connection.trackIncoming(ChunkDataPacket.class);
        player.addPacketToQueue(new ClientPlayerPositionPacket(new Vec(-0.5, 40, -0.5), true, false));
        player.interpretPacketQueue();
        chunkDataPacketCollector.assertCount(viewDiameter);

        // Move to next chunk
        chunkDataPacketCollector = connection.trackIncoming(ChunkDataPacket.class);
        player.addPacketToQueue(new ClientPlayerPositionPacket(new Vec(0.5, 40, -0.5), true, false));
        player.interpretPacketQueue();
        chunkDataPacketCollector.assertCount(viewDiameter);

        // Move to next chunk
        chunkDataPacketCollector = connection.trackIncoming(ChunkDataPacket.class);
        player.addPacketToQueue(new ClientPlayerPositionPacket(new Vec(0.5, 40, 0.5), true, false));
        player.interpretPacketQueue();
        chunkDataPacketCollector.assertEmpty();

        // Move to next chunk
        chunkDataPacketCollector = connection.trackIncoming(ChunkDataPacket.class);
        player.addPacketToQueue(new ClientPlayerPositionPacket(new Vec(0.5, 40, -0.5), true, false));
        player.interpretPacketQueue();
        chunkDataPacketCollector.assertEmpty();

        // Move to next chunk
        chunkDataPacketCollector = connection.trackIncoming(ChunkDataPacket.class);
        // Abuse the fact that there is no delta check
        player.addPacketToQueue(new ClientPlayerPositionPacket(new Vec(16.5, 40, -16.5), true, false));
        player.interpretPacketQueue();
        chunkDataPacketCollector.assertCount(viewDiameter * 2 - 1);
    }

    @Test
    public void testClientViewDistanceSettings(Env env) {
        int viewDistance = 4;
        final Instance flatInstance = env.createFlatInstance();
        var connection = env.createConnection();
        Player player = connection.connect(flatInstance, new Pos(0.5, 40, 0.5));
        // Preload all possible chunks to avoid issues due to async loading
        Set<CompletableFuture<Chunk>> chunks = new HashSet<>();
        ChunkRange.chunksInRange(10, 10, viewDistance + 3, (x, z) -> chunks.add(flatInstance.loadChunk(x, z)));
        CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
        player.refreshSettings(new ClientSettings(
                Locale.US, (byte) viewDistance,
                ChatMessageType.FULL, true,
                (byte) 0, ClientSettings.MainHand.RIGHT,
                false, true,
                ClientSettings.ParticleSetting.ALL
        ));

        Collector<ChunkDataPacket> chunkDataPacketCollector = connection.trackIncoming(ChunkDataPacket.class);
        player.addPacketToQueue(new ClientTeleportConfirmPacket(player.getLastSentTeleportId()));
        player.teleport(new Pos(176, 40, 176));
        player.addPacketToQueue(new ClientTeleportConfirmPacket(player.getLastSentTeleportId()));
        player.addPacketToQueue(new ClientPlayerPositionPacket(new Vec(176.5, 40, 176.5), true, false));
        player.interpretPacketQueue();
        chunkDataPacketCollector.assertCount(ChunkRange.chunksCount(player.effectiveViewDistance()));
    }

    @Test
    public void testSettingsViewDistanceExpansionAndShrink(Env env) {
        byte endViewDistance = 12;
        byte finalViewDistance = 10;
        var instance = env.createFlatInstance();
        instance.viewDistance(20);
        var connection = env.createConnection();
        Pos startingPlayerPos = new Pos(0, 42, 0);
        var player = connection.connect(instance, startingPlayerPos);

        int startingEffective = player.effectiveViewDistance();
        int maxEffective = Math.min(endViewDistance, instance.viewDistance()) + 1;

        // Preload chunks, otherwise our first tracker.assertCount call will fail randomly due to chunks being loaded off the main thread
        ChunkRange.chunksInRange(0, 0, maxEffective, (chunkX, chunkZ) -> instance.loadChunk(chunkX, chunkZ).join());

        var tracker = connection.trackIncoming(ChunkDataPacket.class);
        player.addPacketToQueue(new ClientSettingsPacket(new ClientSettings(Locale.US, endViewDistance,
                ChatMessageType.FULL, false, (byte) 0, ClientSettings.MainHand.RIGHT,
                false, true, ClientSettings.ParticleSetting.ALL)));
        player.interpretPacketQueue();
        int expandedEffective = player.effectiveViewDistance();
        int expectedLoadedChunks = ChunkRange.chunksCount(expandedEffective) - ChunkRange.chunksCount(startingEffective);
        tracker.assertCount(expectedLoadedChunks);

        var tracker1 = connection.trackIncoming(UnloadChunkPacket.class);
        int previousEffective = player.effectiveViewDistance();
        player.addPacketToQueue(new ClientSettingsPacket(new ClientSettings(Locale.US, finalViewDistance,
                ChatMessageType.FULL, false, (byte) 0, ClientSettings.MainHand.RIGHT,
                false, true, ClientSettings.ParticleSetting.ALL)));
        player.interpretPacketQueue();

        int shrunkEffective = player.effectiveViewDistance();
        int expectedUnloadedChunks = ChunkRange.chunksCount(previousEffective) - ChunkRange.chunksCount(shrunkEffective);
        tracker1.assertCount(expectedUnloadedChunks);
    }

    @Test
    public void testCancelledMove(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var p1 = connection.connect(instance, new Pos(0, 40, 0));
        p1.refreshReceivedTeleportId(p1.getLastSentTeleportId()); // Don't care about teleport confirm from spawn

        instance.eventNode().addListener(PlayerMoveEvent.class, event -> event.setCancelled(true));
        var collector = connection.trackIncoming(PlayerPositionAndLookPacket.class);

        p1.addPacketToQueue(new ClientPlayerPositionPacket(new Pos(0.2, 40, 0), true, false));
        p1.interpretPacketQueue();

        assertEquals(new Pos(0, 40, 0), p1.getPosition());
        collector.assertSingle(packet -> {
            assertEquals(0, packet.flags());
            assertEquals(new Vec(0, 40, 0), packet.position().asVec());
            // Must reset velocity or the player will keep moving and create a loop of teleport cancel teleport.
            assertEquals(Vec.ZERO, packet.delta());
        });
    }
}
