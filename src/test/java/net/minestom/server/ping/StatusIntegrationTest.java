package net.minestom.server.ping;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.player.ClientSettings;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnvTest
public class StatusIntegrationTest {

    @Test
    void testPlayerInfoSamples(Env env) {
        var instance = env.createEmptyInstance();
        env.createPlayer(instance, Pos.ZERO);
        env.createPlayer(instance, Pos.ZERO);
        var player3 = env.createPlayer(instance, Pos.ZERO);
        player3.refreshSettings(new ClientSettings(
                Locale.US, (byte) ServerFlag.CHUNK_VIEW_DISTANCE,
                ChatMessageType.FULL, true,
                (byte) 0x7F, ClientSettings.MainHand.RIGHT,
                true, false,
                ClientSettings.ParticleSetting.ALL
        ));

        var unlimitedInfo = Status.PlayerInfo.online(20);
        assertEquals(4, unlimitedInfo.maxPlayers());
        assertEquals(3, unlimitedInfo.onlinePlayers());
        assertEquals(2, unlimitedInfo.sample().size());

        var containsHiddenPlayer = unlimitedInfo.sample().stream()
                .anyMatch(entry -> entry.getUuid().equals(player3.getUuid()));
        assertFalse(containsHiddenPlayer);

        var limitedInfo = Status.PlayerInfo.online(1);
        assertEquals(1, limitedInfo.sample().size());
    }
}
