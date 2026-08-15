package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientGenerateStructurePacket(Point blockPosition,
                                            int level, boolean keepJigsaws) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientGenerateStructurePacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, ClientGenerateStructurePacket::blockPosition,
            VAR_INT, ClientGenerateStructurePacket::level,
            BOOLEAN, ClientGenerateStructurePacket::keepJigsaws,
            ClientGenerateStructurePacket::new);
}
