package net.minestom.server.ping;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.MainHand;
import net.minestom.server.listener.preplay.StatusListener;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.network.player.ClientSettings;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnvTest
public class StatusIntegrationTest {

    @Test
    void statusPacketLengthUsesPreAuthLimit() {
        assertEquals(ServerFlag.MAX_PACKET_SIZE_PRE_AUTH,
                PacketReading.maxPacketSize(ConnectionState.STATUS));

        final NetworkBuffer buffer = NetworkBuffer.staticBuffer(ServerFlag.POOLED_BUFFER_SIZE);
        buffer.write(NetworkBuffer.VAR_INT, ServerFlag.MAX_PACKET_SIZE_PRE_AUTH + 1);

        assertThrows(DataFormatException.class,
                () -> PacketReading.readClients(buffer, ConnectionState.STATUS, false));
    }

    @Test
    void statusRequestOnlyRespondsOnce() throws InterruptedException {
        final TestConnection connection = new TestConnection();
        connection.setClientState(ConnectionState.STATUS);

        final Thread readThread = Thread.startVirtualThread(() -> {
            StatusListener.requestListener(new StatusRequestPacket(), connection);
            StatusListener.requestListener(new StatusRequestPacket(), connection);
        });
        readThread.join();

        assertFalse(connection.isOnline());
        assertEquals(1, connection.packets.size());
        assertInstanceOf(ResponsePacket.class, connection.packets.getFirst());
    }

    @Test
    void testPlayerInfoSamples(Env env) {
        var instance = env.createEmptyInstance();
        env.createPlayer(instance, Pos.ZERO);
        env.createPlayer(instance, Pos.ZERO);
        var player3 = env.createPlayer(instance, Pos.ZERO);
        player3.refreshSettings(new ClientSettings(
                Locale.US, (byte) ServerFlag.CHUNK_VIEW_DISTANCE,
                ChatMessageType.FULL, true,
                (byte) 0x7F, MainHand.RIGHT,
                true, false,
                ClientSettings.ParticleSetting.ALL
        ));

        var unlimitedInfo = Status.PlayerInfo.online(20);
        assertEquals(4, unlimitedInfo.maxPlayers());
        assertEquals(3, unlimitedInfo.onlinePlayers());
        assertEquals(2, unlimitedInfo.sample().size());

        boolean containsHiddenPlayer = unlimitedInfo.sample().stream()
                .anyMatch(entry -> entry.getUuid().equals(player3.getUuid()));
        assertFalse(containsHiddenPlayer);

        var limitedInfo = Status.PlayerInfo.online(1);
        assertEquals(1, limitedInfo.sample().size());
    }

    private static final class TestConnection extends PlayerConnection {
        private final List<SendablePacket> packets = new ArrayList<>();

        @Override
        public void sendPacket(SendablePacket packet) {
            packets.add(packet);
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return new InetSocketAddress(InetAddress.getLoopbackAddress(), 25565);
        }
    }
}
