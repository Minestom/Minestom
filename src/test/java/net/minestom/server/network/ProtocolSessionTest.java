package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.*;

public class ProtocolSessionTest {

    @Test
    public void readPackets() throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);
        var session = ProtocolSession.builder(MinecraftServer.process()).build();
        session.clientState(ConnectionState.PLAY);

        PacketWriting.writeFramedPacket(session.readBuffer(), ConnectionState.PLAY, packet, 0);

        var result = session.readPackets(PacketVanilla.CLIENT_PACKET_PARSER);
        if (!(result instanceof PacketReading.Result.Success<ClientPacket> success)) {
            fail("Expected a success result, got " + result);
            return;
        }
        List<PacketReading.ParsedPacket<ClientPacket>> packets = success.packets();
        assertEquals(List.of(packet), packets.stream().map(PacketReading.ParsedPacket::packet).toList());
        assertEquals(0, session.readBuffer().readableBytes());
    }

    @Test
    public void clientStateAdvancesOnRead() throws DataFormatException {
        var packet = new ClientHandshakePacket(MinecraftServer.PROTOCOL_VERSION, "localhost", 25565,
                ClientHandshakePacket.Intent.LOGIN);
        var session = ProtocolSession.builder(MinecraftServer.process()).build();

        PacketWriting.writeFramedPacket(session.readBuffer(), ConnectionState.HANDSHAKE, packet, 0);

        assertInstanceOf(PacketReading.Result.Success.class, session.readPackets(PacketVanilla.CLIENT_PACKET_PARSER));
        assertEquals(ConnectionState.LOGIN, session.clientState());
        assertEquals(ConnectionState.LOGIN, session.serverState());
    }

    @Test
    public void resizesOnReadFailure() throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);
        var session = ProtocolSession.builder(MinecraftServer.process())
                .readBufferSize(128)
                .build();

        NetworkBuffer framed = NetworkBuffer.resizableBuffer(MinecraftServer.process());
        PacketWriting.writeFramedPacket(framed, ConnectionState.PLAY, packet, 0);
        NetworkBuffer.copy(framed, 0, session.readBuffer(), 0, 128);
        session.readBuffer().writeIndex(128);
        session.clientState(ConnectionState.PLAY);

        var result = session.readPackets(PacketVanilla.CLIENT_PACKET_PARSER);

        assertInstanceOf(PacketReading.Result.Failure.class, result);
        assertEquals(framed.writeIndex(), session.readBuffer().capacity());
    }

    @Test
    public void flushWritesQueuedPackets() throws IOException, DataFormatException {
        var packet = new SystemChatPacket(Component.text("Hello World!"), false);
        var session = ProtocolSession.builder(MinecraftServer.process()).build();
        session.serverState(ConnectionState.PLAY);
        session.send(packet);

        try (SocketPair pair = SocketPair.open()) {
            assertTrue(session.flushTo(pair.client()));

            NetworkBuffer buffer = NetworkBuffer.resizableBuffer(MinecraftServer.process());
            assertTrue(buffer.readChannel(pair.server()) > 0);

            var result = PacketReading.readServers(buffer, ConnectionState.PLAY, false);
            if (!(result instanceof PacketReading.Result.Success<?> success)) {
                fail("Expected a success result, got " + result);
                return;
            }
            assertEquals(List.of(packet), success.packets().stream().map(PacketReading.ParsedPacket::packet).toList());
        }
    }

    @Test
    public void serverStateAdvancesOnFlush() throws IOException {
        var session = ProtocolSession.builder(MinecraftServer.process()).build();
        session.serverState(ConnectionState.CONFIGURATION);
        session.send(new FinishConfigurationPacket());

        try (SocketPair pair = SocketPair.open()) {
            assertTrue(session.flushTo(pair.client()));
        }

        assertEquals(ConnectionState.PLAY, session.serverState());
    }

    private record SocketPair(SocketChannel client, SocketChannel server) implements AutoCloseable {
        private static SocketPair open() throws IOException {
            try (ServerSocketChannel listener = ServerSocketChannel.open()) {
                listener.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
                SocketChannel client = SocketChannel.open(listener.getLocalAddress());
                SocketChannel server = listener.accept();
                return new SocketPair(client, server);
            }
        }

        @Override
        public void close() throws IOException {
            client.close();
            server.close();
        }
    }
}
