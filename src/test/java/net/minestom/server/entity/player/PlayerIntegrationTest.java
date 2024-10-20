package net.minestom.server.entity.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.player.PlayerGameModeChangeEvent;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.packet.client.common.ClientSettingsPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.ClientSettings;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class PlayerIntegrationTest {

    /**
     * Test to see whether player abilities are updated correctly and events
     * are handled properly when changing gamemode.
     */
    @Test
    public void gamemodeTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        // Abilities
        {
            player.setGameMode(GameMode.CREATIVE);
            assertAbilities(player, true, false, true, true);
            player.setGameMode(GameMode.SPECTATOR);
            assertAbilities(player, true, true, true, false);
            player.setGameMode(GameMode.CREATIVE);
            assertAbilities(player, true, true, true, true);
            player.setGameMode(GameMode.ADVENTURE);
            assertAbilities(player, false, false, false, false);
            player.setGameMode(GameMode.SURVIVAL);
            assertAbilities(player, false, false, false, false);
        }

        var listener = env.listen(PlayerGameModeChangeEvent.class);
        // Normal change
        {
            listener.followup();
            assertTrue(player.setGameMode(GameMode.ADVENTURE));
        }
        // Change target gamemode event
        {
            listener.followup(event -> event.setNewGameMode(GameMode.SPECTATOR));
            assertTrue(player.setGameMode(GameMode.CREATIVE));
            assertEquals(GameMode.SPECTATOR, player.getGameMode());
        }
        // Cancel event
        {
            listener.followup(event -> event.setCancelled(true));
            assertFalse(player.setGameMode(GameMode.CREATIVE));
            assertEquals(GameMode.SPECTATOR, player.getGameMode());
        }
    }

    @Test
    public void handSwapTest(Env env) {
        ClientSettingsPacket packet = new ClientSettingsPacket(new ClientSettings(
                Locale.US, (byte) 16,
                ChatMessageType.FULL, true,
                (byte) 127, ClientSettings.MainHand.LEFT,
                true, true,
                ClientSettings.ParticleSetting.ALL
        ));

        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());
        env.tick();
        env.tick();

        player.addPacketToQueue(packet);
        var collector = connection.trackIncoming();
        env.tick();
        env.tick();
        assertEquals(ClientSettings.MainHand.LEFT, player.getSettings().mainHand());

        boolean found = false;
        for (ServerPacket serverPacket : collector.collect()) {
            if (!(serverPacket instanceof EntityMetaDataPacket metaDataPacket)) {
                continue;
            }
            assertEquals((byte) 0, metaDataPacket.entries().get(18).value(),
                    "EntityMetaDataPacket has the incorrect hand after client settings update.");
            found = true;
        }
        assertTrue(found, "EntityMetaDataPacket not sent after client settings update.");

        assertEquals(ClientSettings.ParticleSetting.ALL, player.getSettings().particleSetting());
    }

    private void assertAbilities(Player player, boolean isInvulnerable, boolean isFlying, boolean isAllowFlying,
                                 boolean isInstantBreak) {
        assertEquals(isInvulnerable, player.isInvulnerable());
        assertEquals(isFlying, player.isFlying());
        assertEquals(isAllowFlying, player.isAllowFlying());
        assertEquals(isInstantBreak, player.isInstantBreak());
    }

    @Test
    public void playerJoinPackets(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();

        final var packets = List.of(
                JoinGamePacket.class, ServerDifficultyPacket.class, SpawnPositionPacket.class,
                DeclareCommandsPacket.class, EntityAttributesPacket.class, EntityStatusPacket.class,
                UpdateHealthPacket.class, PlayerAbilitiesPacket.class
        );
        final List<Collector<?>> trackers = new ArrayList<>();
        for (var packet : packets) {
            trackers.add(connection.trackIncoming(packet));
        }

        var trackerAll = connection.trackIncoming(ServerPacket.class);

        var player = connection.connect(instance, new Pos(0, 40, 0));
        assertEquals(instance, player.getInstance());
        assertEquals(new Pos(0, 40, 0), player.getPosition());

        for (var tracker : trackers) {
            assertEquals(1, tracker.collect().size());
        }
        assertTrue(trackerAll.collect().size() > packets.size());
    }

    /**
     * Test to see whether the packets from Player#refreshPlayer are sent
     * when changing dimensions
     */
    @Test
    public void refreshPlayerTest(Env env) {
        final int TEST_PERMISSION_LEVEL = 2;
        final var testDimension = env.process().dimensionType().register(NamespaceID.from("minestom:test_dimension"), DimensionType.builder().build());

        var instance = env.createFlatInstance();
        var instance2 = env.process().instance().createInstanceContainer(testDimension);

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        var tracker1 = connection.trackIncoming(UpdateHealthPacket.class);
        var tracker2 = connection.trackIncoming(SetExperiencePacket.class);
        var trackerStatus = connection.trackIncoming(EntityStatusPacket.class);
        var tracker4 = connection.trackIncoming(PlayerAbilitiesPacket.class);

        player.setPermissionLevel(TEST_PERMISSION_LEVEL);

        // #join may cause the thread to hang as scheduled for the next tick when initially in a pool
        Assertions.assertTimeout(Duration.ofSeconds(2), () -> player.setInstance(instance2).join());
        assertEquals(instance2, player.getInstance());

        assertEquals(1, tracker1.collect().size());
        assertEquals(1, tracker2.collect().size());
        assertEquals(2, trackerStatus.collect().size());
        assertEquals(1, tracker4.collect().size());

        // Ensure that the player was sent the permission levels
        for (var statusPacket : trackerStatus.collect()) {
            assertEquals(player.getEntityId(), statusPacket.entityId());
            assertEquals(24 + TEST_PERMISSION_LEVEL, statusPacket.status()); // TODO: Remove magic value of 24
        }
    }

    @Test
    public void deathLocationTest(Env env) {
        String dimensionNamespace = "minestom:test_dimension";
        final var testDimension = env.process().dimensionType().register(NamespaceID.from(dimensionNamespace), DimensionType.builder().build());

        var instance = env.process().instance().createInstanceContainer(testDimension);
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(5, 42, 2));

        assertNull(player.getDeathLocation());
        player.damage(DamageType.OUT_OF_WORLD, 30);

        assertNotNull(player.getDeathLocation());
        assertEquals(dimensionNamespace, player.getDeathLocation().dimension());
        assertEquals(5, player.getDeathLocation().blockPosition().x());
    }

    @Test
    public void displayNameTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var tracker = connection.trackIncoming(PlayerInfoUpdatePacket.class);
        var player = connection.connect(instance, new Pos(0, 42, 0));

        player.setDisplayName(Component.text("Display Name!"));

        var connection2 = env.createConnection();
        var tracker2 = connection2.trackIncoming(PlayerInfoUpdatePacket.class);
        connection2.connect(instance, new Pos(0, 42, 0));

        var displayNamePackets = tracker2.collect().stream().filter((packet) ->
                        packet.actions().stream().anyMatch((act) -> act == PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME))
                .count();
        assertEquals(1, displayNamePackets);

        var tracker3 = connection2.trackIncoming(PlayerInfoUpdatePacket.class);

        player.setDisplayName(Component.text("Other Name!"));

        var displayNamePackets2 = tracker3.collect().stream().filter((packet) ->
                        packet.actions().stream().anyMatch((act) -> act == PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME))
                .count();
        assertEquals(1, displayNamePackets2);

        var displayNamePackets3 = tracker.collect().stream().filter((packet) ->
                        packet.actions().stream().anyMatch((act) -> act == PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME))
                .count();
        assertEquals(2, displayNamePackets3);
    }

    @Test
    public void setView(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        Pos startingPlayerPos = new Pos(0, 42, 0);
        var player = connection.connect(instance, startingPlayerPos);

        var tracker = connection.trackIncoming(PlayerPositionAndLookPacket.class);
        player.setView(30, 20);

        assertEquals(startingPlayerPos.withView(30, 20), player.getPosition());
        tracker.assertSingle(PlayerPositionAndLookPacket.class, packet -> {
            assertEquals(RelativeFlags.COORD, packet.flags());
            assertEquals(packet.position(), new Pos(0, 0, 0, 30, 20));
        });
    }

    @Test
    public void lookAt(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var tracker = connection.trackIncoming(FacePlayerPacket.class);
        Pos startingPlayerPos = new Pos(0, 42, 0);
        var player = connection.connect(instance, startingPlayerPos);

        Point pointLookAt = new Vec(3, 3, 3);
        player.lookAt(pointLookAt);
        tracker.assertSingle(FacePlayerPacket.class, packet -> assertEquals(pointLookAt, packet.target()));

        tracker = connection.trackIncoming(FacePlayerPacket.class);
        Entity entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(player.getInstance(), new Pos(9, 9, 9));
        player.lookAt(entity);
        tracker.assertSingle(FacePlayerPacket.class, packet -> assertEquals(entity.getEntityId(), packet.entityId()));

        assertEquals(startingPlayerPos, player.getPosition());
    }
}
