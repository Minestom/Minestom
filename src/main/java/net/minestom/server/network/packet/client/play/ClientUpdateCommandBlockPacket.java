package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientUpdateCommandBlockPacket(Point blockPosition, String command,
                                             Mode mode, byte flags) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientUpdateCommandBlockPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, ClientUpdateCommandBlockPacket::blockPosition,
            STRING, ClientUpdateCommandBlockPacket::command,
            Mode.NETWORK_TYPE, ClientUpdateCommandBlockPacket::mode,
            BYTE, ClientUpdateCommandBlockPacket::flags,
            ClientUpdateCommandBlockPacket::new);

    public enum Mode {
        SEQUENCE,
        AUTO,
        REDSTONE;

        public static final NetworkBuffer.Type<Mode> NETWORK_TYPE = NetworkBuffer.Enum(Mode.class);
    }
}
