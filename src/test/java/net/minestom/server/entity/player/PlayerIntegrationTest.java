package net.minestom.server.entity.player;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class PlayerIntegrationTest {

    /**
     * Test to see whether player abilities are updated correctly when changing gamemodes
     */
    @Test
    public void gamemodeTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

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

    private void assertAbilities(Player player, boolean isInvulnerable, boolean isFlying, boolean isAllowFlying,
                                 boolean isInstantBreak) {
        assertEquals(isInvulnerable, player.isInvulnerable());
        assertEquals(isFlying, player.isFlying());
        assertEquals(isAllowFlying, player.isAllowFlying());
        assertEquals(isInstantBreak, player.isInstantBreak());
    }

    /**
     * Test to see whether the packets from Player#refreshPlayer are sent
     * when changing dimensions
     */
    @Test
    public void refreshPlayerTest(Env env) {
        final int TEST_PERMISSION_LEVEL = 2;
        final var testDimension = DimensionType.builder(NamespaceID.from("minestom:test_dimension")).build();
        env.process().dimension().addDimension(testDimension);

        var instance = env.createFlatInstance();
        var instance2 = env.process().instance().createInstanceContainer(testDimension);

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
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

}
