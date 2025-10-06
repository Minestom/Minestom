package net.minestom.server.network.packet;

import net.minestom.server.network.debug.DebugSubscription;
import net.minestom.server.network.packet.client.play.ClientDebugSubscriptionRequestPacket;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class ClientDebugSubscriptionRequestPacketTest {

    @Test
    void testUnmodifiable() {
        var packet = new ClientDebugSubscriptionRequestPacket(new HashSet<>());
        assertThrows(UnsupportedOperationException.class, () -> packet.subscriptions().add(DebugSubscription.POIS));
    }
}
