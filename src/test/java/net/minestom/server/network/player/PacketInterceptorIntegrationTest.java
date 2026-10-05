package net.minestom.server.network.player;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientKeepAlivePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class PacketInterceptorIntegrationTest {

    @BeforeAll
    public static void setup(Env env) { // PACKET_POOL
        Assertions.assertNotNull(env.process().registries());
    }

    private static NetworkBuffer framed(ClientKeepAlivePacket packet, boolean compressed) {
        var buffer = PacketVanilla.PACKET_POOL.get();
        buffer.readIndex(0);
        buffer.writeIndex(0);
        PacketWriting.writeFramedPacket(buffer, PacketVanilla.CLIENT_PACKET_PARSER,
                ConnectionState.PLAY, packet, compressed ? 256 : 0);
        return buffer;
    }

    private static List<ClientPacket> success(PacketReading.Result<ClientPacket> result) {
        assertInstanceOf(PacketReading.Result.Success.class, result, "Expected success, got " + result);
        var packets = ((PacketReading.Result.Success<ClientPacket>) result).packets()
                .stream().map(PacketReading.ParsedPacket::packet).toList();
        assertTrue(!packets.isEmpty());
        return packets;
    }

    private static PacketReading.FrameHook hook(PacketReading.FrameHook.FrameResult verdict,
                                                AtomicBoolean called) {
        return new PacketReading.FrameHook() {
            @Override
            public FrameResult transform(ConnectionState state, NetworkBuffer frame) {
                called.set(true);
                // Rewrite the keep-alive id in place, past the packet id
                frame.read(NetworkBuffer.VAR_INT);
                frame.writeAt(frame.readIndex(), NetworkBuffer.LONG, 5678L);
                frame.readIndex(0);
                return verdict;
            }
        };
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void keep(boolean compressed) throws DataFormatException {
        var buffer = framed(new ClientKeepAlivePacket(1234L), compressed);
        var called = new AtomicBoolean();
        var result = PacketReading.readPackets(buffer, PacketVanilla.CLIENT_PACKET_PARSER,
                ConnectionState.PLAY, PacketVanilla::nextClientState, compressed,
                (info, buf) -> info.serializer().read(buf),
                hook(new PacketReading.FrameHook.FrameResult.Keep(), called));
        assertTrue(called.get());
        // Id rewritten in place by the hook
        assertEquals(List.of(new ClientKeepAlivePacket(5678L)), success(result));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void drop(boolean compressed) throws DataFormatException {
        var buffer = framed(new ClientKeepAlivePacket(1234L), compressed);
        var called = new AtomicBoolean();
        var result = PacketReading.readPackets(buffer, PacketVanilla.CLIENT_PACKET_PARSER,
                ConnectionState.PLAY, PacketVanilla::nextClientState, compressed,
                (info, buf) -> info.serializer().read(buf),
                hook(new PacketReading.FrameHook.FrameResult.Drop(), called));
        assertTrue(called.get());
        assertInstanceOf(PacketReading.Result.Skipped.class, result, "Expected skipped, got " + result);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void replace(boolean compressed) throws DataFormatException {
        var buffer = framed(new ClientKeepAlivePacket(1234L), compressed);
        var called = new AtomicBoolean();
        // Replacement [packetId + payload] for another keep-alive
        var replacement = NetworkBuffer.resizableBuffer();
        var registry = PacketVanilla.CLIENT_PACKET_PARSER.stateRegistry(ConnectionState.PLAY);
        replacement.write(NetworkBuffer.VAR_INT, registry.packetInfo(ClientKeepAlivePacket.class).id());
        replacement.write(NetworkBuffer.LONG, 9999L);
        var result = PacketReading.readPackets(buffer, PacketVanilla.CLIENT_PACKET_PARSER,
                ConnectionState.PLAY, PacketVanilla::nextClientState, compressed,
                (info, buf) -> info.serializer().read(buf),
                new PacketReading.FrameHook() {
                    @Override
                    public FrameResult transform(ConnectionState state, NetworkBuffer frame) {
                        called.set(true);
                        return new FrameResult.Replace(replacement);
                    }
                });
        assertTrue(called.get());
        assertEquals(List.of(new ClientKeepAlivePacket(9999L)), success(result));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void filterSkipsHook(boolean compressed) throws DataFormatException {
        var buffer = framed(new ClientKeepAlivePacket(1234L), compressed);
        var called = new AtomicBoolean();
        var result = PacketReading.readPackets(buffer, PacketVanilla.CLIENT_PACKET_PARSER,
                ConnectionState.PLAY, PacketVanilla::nextClientState, compressed,
                (info, buf) -> info.serializer().read(buf),
                new PacketReading.FrameHook() {
                    @Override
                    public boolean intercepts(int packetId) {
                        return false;
                    }

                    @Override
                    public FrameResult transform(ConnectionState state, NetworkBuffer frame) {
                        called.set(true);
                        return new FrameResult.Keep();
                    }
                });
        assertTrue(!called.get());
        assertEquals(List.of(new ClientKeepAlivePacket(1234L)), success(result));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void peekedIdIsPacketId(boolean compressed) throws DataFormatException {
        var buffer = framed(new ClientKeepAlivePacket(1234L), compressed);
        int expectedId = PacketVanilla.CLIENT_PACKET_PARSER.stateRegistry(ConnectionState.PLAY)
                .packetInfo(ClientKeepAlivePacket.class).id();
        var seen = new AtomicInteger(-1);
        PacketReading.readPackets(buffer, PacketVanilla.CLIENT_PACKET_PARSER,
                ConnectionState.PLAY, PacketVanilla::nextClientState, compressed,
                (info, buf) -> info.serializer().read(buf),
                new PacketReading.FrameHook() {
                    @Override
                    public boolean intercepts(int packetId) {
                        seen.set(packetId);
                        return false;
                    }
                });
        // With compression the wire frame starts with the data length:
        // the hook must still observe the packet id, not the data length.
        assertEquals(expectedId, seen.get());
    }
}
