package net.minestom.server.entity.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.OutgoingTransferEvent;
import net.minestom.server.network.packet.server.common.TransferPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@EnvTest
public class PlayerTransferOutIntegrationTest {

    @Test
    public void testPlayerTransferOut(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, Pos.ZERO);
        var tracker = connection.trackIncoming(TransferPacket.class);

        player.getPlayerConnection().transfer("example.com", 25565);

        tracker.assertSingle(packet -> {
            Assertions.assertEquals("example.com", packet.host());
            Assertions.assertEquals(25565, packet.port());
        });
    }


    @Test
    public void testPlayerTransferOutEvent(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, Pos.ZERO);

        env.listen(OutgoingTransferEvent.class).followup(event -> {
            Assertions.assertEquals(player, event.getPlayer());
            Assertions.assertEquals("example.com", event.getHost());
            Assertions.assertEquals(25565, event.getPort());
        });;

        player.getPlayerConnection().transfer("example.com", 25565);
    }


    @Test
    public void testPlayerTransferOutEventCancelled(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, Pos.ZERO);

        env.process().eventHandler().addListener(OutgoingTransferEvent.class, event -> event.setCancelled(true));

        player.getPlayerConnection().transfer("example.com", 25565);
        connection.trackIncoming(TransferPacket.class).assertEmpty();
    }
}
