package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class SendablePacketIntegrationTest {

    @Test
    public void cached(Env env) {
        var packet = new SystemChatPacket(Component.text("Hello World!"), false);
        var cached = new CachedPacket(packet);
        assertSame(packet, cached.packet(ConnectionState.PLAY));

        var buffer = PacketWriting.allocateTrimmedPacket(ConnectionState.PLAY, packet,
                MinecraftServer.getCompressionThreshold(), env.process().registries());
        var cachedBuffer = cached.body(ConnectionState.PLAY);
        assertTrue(NetworkBuffer.equals(buffer, cachedBuffer));
        // May fail in the very unlikely case where soft references are cleared
        // Rare enough to make this test worth it
        assertSame(cached.body(ConnectionState.PLAY), cachedBuffer);

        assertSame(packet, cached.packet(ConnectionState.PLAY));
    }
}
