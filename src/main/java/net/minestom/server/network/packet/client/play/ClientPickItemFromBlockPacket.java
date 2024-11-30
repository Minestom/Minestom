package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientPickItemFromBlockPacket(@NotNull Point pos, boolean includeData) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPickItemFromBlockPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, ClientPickItemFromBlockPacket::pos,
            BOOLEAN, ClientPickItemFromBlockPacket::includeData,
            ClientPickItemFromBlockPacket::new);
}
