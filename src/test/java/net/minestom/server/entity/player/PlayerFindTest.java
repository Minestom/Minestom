package net.minestom.server.entity.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class PlayerFindTest {

    private final GameProfile[] PROFILES = {
            new GameProfile(UUID.randomUUID(), "Minestom"),
            new GameProfile(UUID.randomUUID(), "Notch")};

    static {
        MinecraftServer.init();
    }

    @Test
    public void testPartialFind(Env env) {
        Instance instance = env.createEmptyInstance();
        Player minestomPlayer = createPlayer(PROFILES[0], instance, env);
        ConnectionManager connectionManager = env.process().connection();

        assertEquals(minestomPlayer, connectionManager.findOnlinePlayer("Mine"));
        assertNull(connectionManager.findOnlinePlayer("No"));

        Player notchPlayer = createPlayer(PROFILES[1], instance, env);

        assertEquals(minestomPlayer, connectionManager.findOnlinePlayer("Mine"));
        assertEquals(notchPlayer, connectionManager.findOnlinePlayer("No"));
        assertNull(connectionManager.findOnlinePlayer("leo"));
    }

    private Player createPlayer(GameProfile profile, Instance instance, Env env) {
        return env.createConnection(profile).connect(instance, new Pos(0, 0, 0));
    }

}
