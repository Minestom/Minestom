package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientUpdateCommandBlockPacket(@NotNull Point blockPosition, @NotNull String command,
                                             @NotNull Mode mode, byte flags) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientUpdateCommandBlockPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, ClientUpdateCommandBlockPacket::blockPosition,
            STRING, ClientUpdateCommandBlockPacket::command,
            Enum(Mode.class), ClientUpdateCommandBlockPacket::mode,
            BYTE, ClientUpdateCommandBlockPacket::flags,
            ClientUpdateCommandBlockPacket::new);

    public enum Mode {
        SEQUENCE, AUTO, REDSTONE
    }
}
