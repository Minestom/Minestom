package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientAnimationPacket;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
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
    public void cached(Env env) {
        var packet = new SystemChatPacket(Component.text("Hello World!"), false);
        var cached = new CachedPacket(packet);
        var packetWriter = env.process().packetWriter();
        assertSame(packet, cached.packet(ConnectionState.PLAY, packetWriter));

        var buffer = PacketWriting.allocateTrimmedPacket(ConnectionState.PLAY, packet,
                MinecraftServer.getCompressionThreshold());
        var cachedBuffer = cached.body(ConnectionState.PLAY, packetWriter);
        assertTrue(NetworkBuffer.contentEquals(buffer, cachedBuffer));
        // May fail in the very unlikely case where soft references are cleared
        // Rare enough to make this test worth it
        assertSame(cached.body(ConnectionState.PLAY, packetWriter), cachedBuffer);

        assertSame(packet, cached.packet(ConnectionState.PLAY, packetWriter));
    }

    @Test
    public void trimmed(Env ignored) throws DataFormatException {
        var packet = new ClientAnimationPacket(PlayerHand.MAIN);

        var buffer = PacketWriting.allocateTrimmedPacket(ConnectionState.PLAY, packet, 0);

        var result = PacketReading.readClient(buffer, ConnectionState.PLAY, false);
        if (!(result instanceof PacketReading.Result.Success<ClientPacket> success)) {
            fail();
            return;
        }
        assertEquals(1, success.packets().size());
        ClientPacket readPacket = success.packets().getFirst().packet();
        assertEquals(packet, readPacket);
    }
}
