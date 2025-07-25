package net.minestom.server.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerGameModeRequestEvent;
import net.minestom.server.network.packet.client.play.ClientChangeGameModePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class PacketListenerIntegrationTest {

    @ParameterizedTest(name = "{0} vs {1}")
    @MethodSource("gameModePairs")
    public void testGameModeSwitchSame(GameMode expectedGamemode, GameMode playerGameMode, Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 41, 0));
        player.setGameMode(playerGameMode);
        var listener = env.listen(PlayerGameModeRequestEvent.class);
        listener.followup(event ->
                assertEquals(expectedGamemode, event.getRequestedGameMode())
        );
        var packet = new ClientChangeGameModePacket(expectedGamemode);
        player.addPacketToQueue(packet);
        player.tick(0);
    }

    // Junit does not support @EnumSource with the same enum value for both
    private static Stream<Arguments> gameModePairs() {
        return Stream.of(GameMode.values())
                .flatMap(a -> Stream.of(GameMode.values()).map(b -> Arguments.of(a, b)));
    }
}
