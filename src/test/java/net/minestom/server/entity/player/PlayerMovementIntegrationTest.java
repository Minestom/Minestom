package net.minestom.server.entity.player;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.MainHand;
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
                (byte) 0, MainHand.RIGHT,
                false, true,
                ClientSettings.ParticleSetting.ALL
        ));

        Collector<ChunkDataPacket> chunkDataPacketCollector = connection.trackIncoming(ChunkDataPacket.class);
        player.addPacketToQueue(new ClientTeleportConfirmPacket(player.getLastSentTeleportId()));
        player.teleport(new Pos(176, 40, 176)).join();
        player.addPacketToQueue(new ClientTeleportConfirmPacket(player.getLastSentTeleportId()));
        player.addPacketToQueue(new ClientPlayerPositionPacket(new Vec(176.5, 40, 176.5), true, false));
        player.interpretPacketQueue();
        chunkDataPacketCollector.assertCount(ChunkRange.chunksCount(player.effectiveViewDistance()));
    }

    @Test
    public void testSettingsViewDistanceExpansionAndShrink(Env env) {
        var instance = env.createFlatInstance();
        // Keep the client view distances used here below the instance's so they are not capped; refreshSettings,
        // like spawn and movement, bounds chunks by min(viewDistance, instanceViewDistance) + 1
        instance.viewDistance(32);
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        // Preload chunks, otherwise our first assertCount call will fail randomly due to chunks being loaded off the main thread
        int maxEffective = Math.min(12, instance.viewDistance()) + 1;
        ChunkRange.chunksInRange(0, 0, maxEffective, (chunkX, chunkZ) -> instance.loadChunk(chunkX, chunkZ).join());

        // Expand the client view distance
        int beforeExpand = player.effectiveViewDistance();
        var added = connection.trackIncoming(ChunkDataPacket.class);
        player.addPacketToQueue(new ClientSettingsPacket(new ClientSettings(Locale.US, (byte) 12,
                ChatMessageType.FULL, false, (byte) 0, MainHand.RIGHT,
                false, true, ClientSettings.ParticleSetting.ALL)));
        player.interpretPacketQueue();
        added.assertCount(ChunkRange.chunksCount(player.effectiveViewDistance()) - ChunkRange.chunksCount(beforeExpand));

        // Shrink the client view distance
        int beforeShrink = player.effectiveViewDistance();
        var removed = connection.trackIncoming(UnloadChunkPacket.class);
        player.addPacketToQueue(new ClientSettingsPacket(new ClientSettings(Locale.US, (byte) 10,
                ChatMessageType.FULL, false, (byte) 0, MainHand.RIGHT,
                false, true, ClientSettings.ParticleSetting.ALL)));
        player.interpretPacketQueue();
        removed.assertCount(ChunkRange.chunksCount(beforeShrink) - ChunkRange.chunksCount(player.effectiveViewDistance()));
    }

    @Test
    public void testSettingsViewDistanceCappedByInstance(Env env) {
        var instance = env.createFlatInstance();
        // Cap the instance's view distance at 4.
        instance.viewDistance(4);
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        // Preload chunks, otherwise our first assertCount call will fail randomly due to chunks being loaded off the main thread
        ChunkRange.chunksInRange(0, 0, 13, (chunkX, chunkZ) -> instance.loadChunk(chunkX, chunkZ).join());

        // The default view distance is 8. Capped at instance view distance (4) + 1 = 5.
        assertEquals(5, player.effectiveViewDistance());

        // Expand settings view distance to 12. Since it is capped by instance view distance 4,
        // the effective view distance remains 5. No chunk packets should be sent.
        var added = connection.trackIncoming(ChunkDataPacket.class);
        var removed = connection.trackIncoming(UnloadChunkPacket.class);
        player.addPacketToQueue(new ClientSettingsPacket(new ClientSettings(Locale.US, (byte) 12,
                ChatMessageType.FULL, false, (byte) 0, MainHand.RIGHT,
                false, true, ClientSettings.ParticleSetting.ALL)));
        player.interpretPacketQueue();
        assertEquals(5, player.effectiveViewDistance());
        added.assertCount(0);
        removed.assertCount(0);

        // Shrink settings view distance to 3. Capped by min(3, 4) + 1 = 4.
        // It shrinks from 5 to 4. We expect UnloadChunkPackets.
        int beforeShrink = player.effectiveViewDistance();
        var removed2 = connection.trackIncoming(UnloadChunkPacket.class);
        player.addPacketToQueue(new ClientSettingsPacket(new ClientSettings(Locale.US, (byte) 3,
                ChatMessageType.FULL, false, (byte) 0, MainHand.RIGHT,
                false, true, ClientSettings.ParticleSetting.ALL)));
        player.interpretPacketQueue();
        assertEquals(4, player.effectiveViewDistance());
        removed2.assertCount(ChunkRange.chunksCount(beforeShrink) - ChunkRange.chunksCount(player.effectiveViewDistance()));

        // Shrink settings view distance to 2. Capped by min(2, 4) + 1 = 3.
        // It shrinks from 4 to 3. We expect UnloadChunkPackets.
        int beforeShrink2 = player.effectiveViewDistance();
        var removed3 = connection.trackIncoming(UnloadChunkPacket.class);
        player.addPacketToQueue(new ClientSettingsPacket(new ClientSettings(Locale.US, (byte) 2,
                ChatMessageType.FULL, false, (byte) 0, MainHand.RIGHT,
                false, true, ClientSettings.ParticleSetting.ALL)));
        player.interpretPacketQueue();
        assertEquals(3, player.effectiveViewDistance());
        removed3.assertCount(ChunkRange.chunksCount(beforeShrink2) - ChunkRange.chunksCount(player.effectiveViewDistance()));

        // Expand settings view distance back to 8. Effective view distance becomes min(8, 4) + 1 = 5.
        // It expands from 3 to 5. We expect ChunkDataPackets.
        int beforeExpand = player.effectiveViewDistance();
        var added2 = connection.trackIncoming(ChunkDataPacket.class);
        player.addPacketToQueue(new ClientSettingsPacket(new ClientSettings(Locale.US, (byte) 8,
                ChatMessageType.FULL, false, (byte) 0, MainHand.RIGHT,
                false, true, ClientSettings.ParticleSetting.ALL)));
        player.interpretPacketQueue();
        assertEquals(5, player.effectiveViewDistance());
        added2.assertCount(ChunkRange.chunksCount(player.effectiveViewDistance()) - ChunkRange.chunksCount(beforeExpand));
    }

    @Test
    public void testDynamicInstanceViewDistanceChange(Env env) {
        var instance = env.createFlatInstance();
        // Start with instance view distance of 4
        instance.viewDistance(4);
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        // Preload chunks up to 13
        ChunkRange.chunksInRange(0, 0, 13, (chunkX, chunkZ) -> instance.loadChunk(chunkX, chunkZ).join());

        // Default settings view distance = 8. Capped at min(8, 4) + 1 = 5.
        assertEquals(5, player.effectiveViewDistance());

        // Expand the instance view distance from 4 to 8.
        // Effective view distance becomes min(8, 8) + 1 = 9.
        // We expect ChunkDataPackets.
        var added = connection.trackIncoming(ChunkDataPacket.class);
        instance.viewDistance(8);
        assertEquals(9, player.effectiveViewDistance());
        added.assertCount(ChunkRange.chunksCount(9) - ChunkRange.chunksCount(5));

        // Shrink the instance view distance from 8 to 3.
        // Effective view distance becomes min(8, 3) + 1 = 4.
        // We expect UnloadChunkPackets.
        var removed = connection.trackIncoming(UnloadChunkPacket.class);
        instance.viewDistance(3);
        assertEquals(4, player.effectiveViewDistance());
        removed.assertCount(ChunkRange.chunksCount(9) - ChunkRange.chunksCount(4));
    }

    @Test
    public void testDynamicInstanceViewDistanceChangeMultiplePlayers(Env env) {
        var instance = env.createFlatInstance();
        instance.viewDistance(4);

        var connection1 = env.createConnection();
        var p1 = connection1.connect(instance, new Pos(0, 42, 0));

        var connection2 = env.createConnection();
        var p2 = connection2.connect(instance, new Pos(0, 42, 0));

        // Preload chunks
        ChunkRange.chunksInRange(0, 0, 13, (chunkX, chunkZ) -> instance.loadChunk(chunkX, chunkZ).join());

        assertEquals(5, p1.effectiveViewDistance());
        assertEquals(5, p2.effectiveViewDistance());

        var added1 = connection1.trackIncoming(ChunkDataPacket.class);
        var added2 = connection2.trackIncoming(ChunkDataPacket.class);

        // Expand view distance
        instance.viewDistance(8);

        assertEquals(9, p1.effectiveViewDistance());
        assertEquals(9, p2.effectiveViewDistance());

        added1.assertCount(ChunkRange.chunksCount(9) - ChunkRange.chunksCount(5));
        added2.assertCount(ChunkRange.chunksCount(9) - ChunkRange.chunksCount(5));
    }

    @Test
    public void testDynamicInstanceViewDistanceChangeIndependent(Env env) {
        var instanceA = env.createFlatInstance();
        instanceA.viewDistance(4);

        var instanceB = env.createFlatInstance();
        instanceB.viewDistance(4);

        var connectionA = env.createConnection();
        var pA = connectionA.connect(instanceA, new Pos(0, 42, 0));

        var connectionB = env.createConnection();
        var pB = connectionB.connect(instanceB, new Pos(0, 42, 0));

        // Preload chunks
        ChunkRange.chunksInRange(0, 0, 13, (chunkX, chunkZ) -> {
            instanceA.loadChunk(chunkX, chunkZ).join();
            instanceB.loadChunk(chunkX, chunkZ).join();
        });

        assertEquals(5, pA.effectiveViewDistance());
        assertEquals(5, pB.effectiveViewDistance());

        var addedA = connectionA.trackIncoming(ChunkDataPacket.class);
        var addedB = connectionB.trackIncoming(ChunkDataPacket.class);

        // Expand view distance of instance A only
        instanceA.viewDistance(8);

        assertEquals(9, pA.effectiveViewDistance());
        assertEquals(5, pB.effectiveViewDistance()); // B should remain unchanged

        addedA.assertCount(ChunkRange.chunksCount(9) - ChunkRange.chunksCount(5));
        addedB.assertCount(0); // B should not receive any chunk packets
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
