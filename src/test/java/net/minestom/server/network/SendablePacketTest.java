package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.server.utils.PacketUtils;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class SendablePacketTest {

    @Test
    public void lazy() {
        var packet = new SystemChatPacket(Component.text("Hello World!"), false);
        AtomicBoolean called = new AtomicBoolean(false);
        var lazy = new LazyPacket(() -> {
            if (called.getAndSet(true))
                fail();
            return packet;
        });
        assertSame(packet, lazy.packet());
        assertSame(packet, lazy.packet());
    }

    @Test
    public void cached() {
        var packet = new SystemChatPacket(Component.text("Hello World!"), false);
        var cached = new CachedPacket(packet);
        assertSame(packet, cached.packet(ConnectionState.PLAY));

        var buffer = PacketUtils.allocateTrimmedPacket(ConnectionState.PLAY, packet);
        var cachedBuffer = cached.body(ConnectionState.PLAY);
        assertTrue(NetworkBuffer.equals(buffer.body(), cachedBuffer));
        // May fail in the very unlikely case where soft references are cleared
        // Rare enough to make this test worth it
        assertSame(cached.body(ConnectionState.PLAY), cachedBuffer);

        assertSame(packet, cached.packet(ConnectionState.PLAY));
    }
}
