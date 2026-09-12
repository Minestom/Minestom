package net.minestom.server.network.player;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientCreativeInventoryActionPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnvTest
final class CreativePacketDecodingIntegrationTest {

    @Test
    void skipsCreativePayloadBeforeDecodingUnlessPlayerIsCreative(Env env) throws Exception {
        try (SocketChannel channel = SocketChannel.open()) {
            final PlayerSocketConnection connection = new PlayerSocketConnection(channel,
                    new InetSocketAddress(0), Thread.currentThread(), Thread.currentThread());
            final Player player = new Player(connection,
                    new GameProfile(UUID.randomUUID(), "CreativeTest"));

            final PacketReading.Result<ClientPacket> skipped = readCreativePacket(connection);
            assertInstanceOf(PacketReading.Result.Skipped.class, skipped);

            player.setGameMode(GameMode.CREATIVE);
            assertThrows(RuntimeException.class, () -> readCreativePacket(connection));
        }
    }

    private static PacketReading.Result<ClientPacket> readCreativePacket(PlayerSocketConnection connection)
            throws Exception {
        final int packetId = PacketVanilla.CLIENT_PACKET_PARSER.play()
                .packetInfo(ClientCreativeInventoryActionPacket.class).id();
        final byte[] payload = NetworkBuffer.makeArray(buffer -> {
            buffer.write(VAR_INT, packetId);
            // Deliberately truncated before the creative-slot short. This would fail if its item were decoded.
            buffer.write(BYTE, (byte) 0);
        });
        final NetworkBuffer frame = NetworkBuffer.resizableBuffer();
        frame.write(VAR_INT, payload.length);
        frame.write(RAW_BYTES, payload);
        return PacketReading.<ClientPacket>readPackets(frame, PacketVanilla.CLIENT_PACKET_PARSER,
                ConnectionState.PLAY, PacketVanilla::nextClientState, false,
                connection::readClientPacket);
    }
}
