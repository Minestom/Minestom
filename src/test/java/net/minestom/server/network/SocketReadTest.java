package net.minestom.server.network;

import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SocketReadTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void complete(boolean compressed) throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(ConnectionState.PLAY, buffer, packet, compressed ? 256 : 0);

        var readResult = PacketReading.readClients(ConnectionState.PLAY, buffer, compressed);
        var packets = readResult.packets();
        assertEquals(0, readResult.missingLength());

        assertEquals(1, packets.size());
        assertEquals(packet, packets.getFirst());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void completeTwo(boolean compressed) throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(ConnectionState.PLAY, buffer, packet, compressed ? 256 : 0);
        PacketWriting.writeFramedPacket(ConnectionState.PLAY, buffer, packet, compressed ? 256 : 0);

        var readResult = PacketReading.readClients(ConnectionState.PLAY, buffer, compressed);
        var packets = readResult.packets();
        assertEquals(0, readResult.missingLength());

        assertEquals(2, packets.size());
        assertEquals(packet, packets.getFirst());
        assertEquals(packet, packets.getLast());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void insufficientLength(boolean compressed) throws DataFormatException {
        // Write a complete packet then the next packet length without any payload

        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(ConnectionState.PLAY, buffer, packet, compressed ? 256 : 0);
        buffer.write(NetworkBuffer.VAR_INT, 200); // incomplete 200 bytes packet

        var readResult = PacketReading.readClients(ConnectionState.PLAY, buffer, compressed);
        var packets = readResult.packets();
        assertEquals(getVarIntSize(200), buffer.readableBytes());
        assertEquals(200, readResult.missingLength());

        assertEquals(1, packets.size());
        assertEquals(packet, packets.getFirst());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void incomplete(boolean compressed) throws DataFormatException {
        // Write a complete packet and incomplete var-int length for the next packet

        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(ConnectionState.PLAY, buffer, packet, compressed ? 256 : 0);
        buffer.write(NetworkBuffer.BYTE, (byte) -85); // incomplete var-int length

        var readResult = PacketReading.readClients(ConnectionState.PLAY, buffer, compressed);
        var packets = readResult.packets();
        assertEquals(1, buffer.readableBytes());
        assertEquals(0, readResult.missingLength());

        assertEquals(1, packets.size());
        assertEquals(packet, packets.getFirst());
    }

    private static int getVarIntSize(int input) {
        return (input & 0xFFFFFF80) == 0
                ? 1 : (input & 0xFFFFC000) == 0
                ? 2 : (input & 0xFFE00000) == 0
                ? 3 : (input & 0xF0000000) == 0
                ? 4 : 5;
    }
}
