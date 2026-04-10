package net.minestom.server.extras.query;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class QueryIntegrationTest {
    private ServerProcess process;

    @BeforeEach
    public void setupProcess() {
        process = MinecraftServer.updateProcess();
        process.start(new InetSocketAddress("127.0.0.1", 0));
    }

    @AfterEach
    public void teardownProcess() {
        if (Query.isStarted()) {
            Query.stop();
        }
        if (process != null) {
            process.stop();
        }
    }

    @Test
    public void truncatedStatPacketDoesNotReusePreviousPayload() throws Exception {
        if (Query.isStarted()) {
            assertTrue(Query.stop());
        }

        final int port = findFreeUdpPort();
        assertTrue(Query.start(port));
        try (DatagramSocket client = new DatagramSocket()) {
            client.setSoTimeout(1000);
            final InetSocketAddress address = new InetSocketAddress("127.0.0.1", port);

            final int sessionId = 0x12345678;

            // Handshake to obtain a valid challenge token.
            send(client, address, ByteBuffer.allocate(7)
                    .putShort((short) 0xFEFD)
                    .put((byte) 9)
                    .putInt(sessionId)
                    .array());
            final int challengeToken = parseChallengeToken(receive(client));

            // Send a valid basic stat request and consume its response.
            send(client, address, ByteBuffer.allocate(11)
                    .putShort((short) 0xFEFD)
                    .put((byte) 0)
                    .putInt(sessionId)
                    .putInt(challengeToken)
                    .array());
            receive(client);

            // Regression case: this packet has no session/challenge fields.
            // Old behavior could read stale bytes from previous packet data and answer anyway.
            send(client, address, new byte[]{(byte) 0xFE, (byte) 0xFD, 0x00});

            assertThrows(SocketTimeoutException.class, () -> {
                DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
                client.receive(packet);
            });
        } finally {
            assertTrue(Query.stop());
        }
    }

    private static int findFreeUdpPort() throws Exception {
        try (DatagramSocket socket = new DatagramSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void send(DatagramSocket client, InetSocketAddress address, byte[] payload) throws Exception {
        client.send(new DatagramPacket(payload, payload.length, address));
    }

    private static DatagramPacket receive(DatagramSocket client) throws Exception {
        DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
        client.receive(packet);
        return packet;
    }

    private static int parseChallengeToken(DatagramPacket packet) {
        byte[] data = packet.getData();
        int length = packet.getLength();
        int offset = packet.getOffset();

        assertTrue(length >= 6, "Handshake response too short");

        ByteBuffer buffer = ByteBuffer.wrap(data, offset, length);
        assertEquals(9, buffer.get(), "Unexpected response type");

        // Session id is echoed back by the protocol.
        buffer.getInt();

        int tokenStart = offset + 5;
        int tokenEnd = tokenStart;
        int packetEnd = offset + length;
        while (tokenEnd < packetEnd && data[tokenEnd] != 0) {
            tokenEnd++;
        }

        String token = new String(data, tokenStart, tokenEnd - tokenStart, StandardCharsets.ISO_8859_1);
        return Integer.parseInt(token);
    }
}
