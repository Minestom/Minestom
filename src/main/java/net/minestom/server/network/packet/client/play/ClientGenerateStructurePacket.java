package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientGenerateStructurePacket(@NotNull Point blockPosition,
                                            int level, boolean keepJigsaws) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientGenerateStructurePacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, ClientGenerateStructurePacket::blockPosition,
            VAR_INT, ClientGenerateStructurePacket::level,
            BOOLEAN, ClientGenerateStructurePacket::keepJigsaws,
            ClientGenerateStructurePacket::new);
}
