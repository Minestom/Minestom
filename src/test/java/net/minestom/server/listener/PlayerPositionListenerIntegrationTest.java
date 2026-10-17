package net.minestom.server.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.ClientTeleportConfirmPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class PlayerPositionListenerIntegrationTest {

    @Test
    public void teleportConfirmAppliesReportedPosition(Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 42, 0));
        player.teleport(new Pos(5, 42, 5)).join();

        Pos reported = new Pos(5.2, 42, 5.1, 90f, 10f);
        player.addPacketToQueue(new ClientTeleportConfirmPacket(player.getLastSentTeleportId(), reported));
        player.interpretPacketQueue();
        assertEquals(reported, player.getPosition());
    }

    @Test
    public void staleTeleportConfirmIgnoresReportedPosition(Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 42, 0));
        player.teleport(new Pos(5, 42, 5)).join();
        int staleId = player.getLastSentTeleportId();
        player.teleport(new Pos(8, 42, 8)).join();

        player.addPacketToQueue(new ClientTeleportConfirmPacket(staleId, new Pos(5.2, 42, 5.1)));
        player.interpretPacketQueue();
        assertEquals(new Pos(8, 42, 8), player.getPosition());
    }
}
