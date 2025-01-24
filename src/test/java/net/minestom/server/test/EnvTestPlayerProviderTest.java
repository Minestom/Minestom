package net.minestom.server.test;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@EnvTest
public class EnvTestPlayerProviderTest {

    public static class CustomPlayer extends Player {
        public CustomPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
            super(playerConnection, gameProfile);
        }
    }

    @Test
    void testPlayerProviderUsedInEnvTest(Env env) {
        // Note: By default the test environment will use a player provider of its own to bypass the queued chunk system
        // overriding in a particular test will mean that chunk packets are not received consistently (they require the
        // chunk queue interaction). However, this is not a problem for many tests, so we do support it.

        env.process().connection().setPlayerProvider(CustomPlayer::new);
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 42, 0));
        assertInstanceOf(CustomPlayer.class, player);
    }
}
