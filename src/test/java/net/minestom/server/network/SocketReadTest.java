package net.minestom.server.network;

import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SocketReadTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void complete(boolean compressed) throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(buffer, ConnectionState.PLAY, packet, compressed ? 256 : 0);

        var readResult = PacketReading.readClients(buffer, ConnectionState.PLAY, compressed);
        if (!(readResult instanceof PacketReading.Result.Success<ClientPacket> success)) {
            throw new AssertionError("Expected a success result, got " + readResult);
        }
        List<ClientPacket> packets = success.packets().stream().map(PacketReading.ParsedPacket::packet).toList();
        assertEquals(List.of(packet), packets);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void completeTwo(boolean compressed) throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(buffer, ConnectionState.PLAY, packet, compressed ? 256 : 0);
        PacketWriting.writeFramedPacket(buffer, ConnectionState.PLAY, packet, compressed ? 256 : 0);

        var readResult = PacketReading.readClients(buffer, ConnectionState.PLAY, compressed);
        if (!(readResult instanceof PacketReading.Result.Success<ClientPacket> success)) {
            throw new AssertionError("Expected a success result, got " + readResult);
        }
        List<ClientPacket> packets = success.packets().stream().map(PacketReading.ParsedPacket::packet).toList();
        assertEquals(List.of(packet, packet), packets);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void insufficientLength(boolean compressed) throws DataFormatException {
        // Write a complete packet then the next packet length without any payload

        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(buffer, ConnectionState.PLAY, packet, compressed ? 256 : 0);
        buffer.write(NetworkBuffer.VAR_INT, 200); // incomplete 200 bytes packet

        var readResult = PacketReading.readClients(buffer, ConnectionState.PLAY, compressed);
        if (!(readResult instanceof PacketReading.Result.Success<ClientPacket> success)) {
            throw new AssertionError("Expected a success result, got " + readResult);
        }
        List<ClientPacket> packets = success.packets().stream().map(PacketReading.ParsedPacket::packet).toList();
        assertEquals(List.of(packet), packets);

        readResult = PacketReading.readClients(buffer, ConnectionState.PLAY, compressed);
        if (!(readResult instanceof PacketReading.Result.Empty<ClientPacket>)) {
            throw new AssertionError("Expected an empty result, got " + readResult);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void incomplete(boolean compressed) throws DataFormatException {
        // Write a complete packet and incomplete var-int length for the next packet

        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(buffer, ConnectionState.PLAY, packet, compressed ? 256 : 0);
        buffer.write(NetworkBuffer.BYTE, (byte) -85); // incomplete var-int length

        var readResult = PacketReading.readClients(buffer, ConnectionState.PLAY, compressed);
        if (!(readResult instanceof PacketReading.Result.Success<ClientPacket> success)) {
            throw new AssertionError("Expected a success result, got " + readResult);
        }
        List<ClientPacket> packets = success.packets().stream().map(PacketReading.ParsedPacket::packet).toList();
        assertEquals(1, buffer.readableBytes());

        assertEquals(List.of(packet), packets);

        // Try to read the next packet
        readResult = PacketReading.readClients(buffer, ConnectionState.PLAY, compressed);
        if (!(readResult instanceof PacketReading.Result.Empty<ClientPacket>)) {
            throw new AssertionError("Expected an empty result, got " + readResult);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void resize(boolean compressed) throws DataFormatException {
        // Write a complete packet that is larger than the buffer capacity

        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(buffer, ConnectionState.PLAY, packet, compressed ? 256 : 0);
        final long packetLength = buffer.writeIndex();
        buffer = buffer.copy(0, packetLength / 2).index(0, packetLength / 2);

        var readResult = PacketReading.readClients(buffer, ConnectionState.PLAY, compressed);
        if (!(readResult instanceof PacketReading.Result.Failure<ClientPacket> failure)) {
            throw new AssertionError("Expected a failure result, got " + readResult);
        }
        assertEquals(packetLength, failure.requiredCapacity());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void resizeHeader(boolean compressed) throws DataFormatException {
        // Write a buffer where you cannot read the packet length

        var buffer = NetworkBuffer.staticBuffer(1);
        buffer.write(NetworkBuffer.BYTE, (byte) -85); // incomplete var-int length

        var readResult = PacketReading.readClients(buffer, ConnectionState.PLAY, compressed);
        if (!(readResult instanceof PacketReading.Result.Failure<ClientPacket> failure)) {
            throw new AssertionError("Expected a failure result, got " + readResult);
        }
        // 5 = max var-int size
        assertEquals(5, failure.requiredCapacity());
    }

    private static int getVarIntSize(int input) {
        return (input & 0xFFFFFF80) == 0
                ? 1 : (input & 0xFFFFC000) == 0
                ? 2 : (input & 0xFFE00000) == 0
                ? 3 : (input & 0xF0000000) == 0
                ? 4 : 5;
    }
}
