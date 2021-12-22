package network;

import net.kyori.adventure.text.Component;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

public class LazyPacketTest {

    @Test
    public void testCache() {
        var packet = new ChatMessagePacket(Component.text("Hello World!"), ChatPosition.CHAT, UUID.randomUUID());
        AtomicBoolean called = new AtomicBoolean(false);
        var lazy = new LazyPacket(() -> {
            if (called.getAndSet(true))
                fail();
            return packet;
        });
        assertSame(packet, lazy.packet());
        assertSame(packet, lazy.packet());
    }
}
