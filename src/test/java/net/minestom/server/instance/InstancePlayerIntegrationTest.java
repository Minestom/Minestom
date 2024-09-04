package net.minestom.server.instance;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
public class InstancePlayerIntegrationTest {

    @Test
    void testInstanceDestroyWithPlayersInside(Env env) {
        Instance instance = env.createFlatInstance();
        for (int i = 0; i < 3; i++) {
            // Default position is Pos.ZERO
            env.createPlayer(instance);
        }

        assertFalse(instance.getPlayers().isEmpty());
        for (Player player : instance.getPlayers()) {
            assertEquals(Pos.ZERO, player.getPosition());
        }
        assertDoesNotThrow(() -> env.destroyInstance(instance, true));
        assertTrue(instance.getPlayers().isEmpty());
    }

    @Test
    void testInstanceDestroyWithExceptionThrow(Env env) {
        Instance instance = env.createFlatInstance();
        for (int i = 0; i < 3; i++) {
            // Default position is Pos.ZERO
            env.createPlayer(instance);
        }
        assertFalse(instance.getPlayers().isEmpty());
        assertThrows(
                IllegalStateException.class,
                () -> env.destroyInstance(instance),
                "You cannot unregister an instance with players inside."
        );
        assertFalse(instance.getPlayers().isEmpty());
        env.destroyInstance(instance, true);
        assertTrue(instance.getPlayers().isEmpty());
    }
}
