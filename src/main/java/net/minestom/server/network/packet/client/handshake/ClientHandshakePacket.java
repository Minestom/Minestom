package net.minestom.server.network.packet.client.handshake;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientHandshakePacket(int protocolVersion, String serverAddress,
                                    int serverPort, Intent intent) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientHandshakePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientHandshakePacket::protocolVersion,
            STRING, ClientHandshakePacket::serverAddress,
            UNSIGNED_SHORT, ClientHandshakePacket::serverPort,
            VAR_INT.transform(Intent::fromId, Intent::id), ClientHandshakePacket::intent,
            ClientHandshakePacket::new);

    public enum Intent {
        STATUS,
        LOGIN,
        TRANSFER;

        public static Intent fromId(int id) {
            return switch (id) {
                case 1 -> STATUS;
                case 2 -> LOGIN;
                case 3 -> TRANSFER;
                default -> throw new IllegalArgumentException("Unknown connection intent: " + id);
            };
        }

        public int id() {
            return ordinal() + 1;
        }
    }
}
