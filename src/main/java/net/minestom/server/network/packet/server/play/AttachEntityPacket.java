package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.INT;

public record AttachEntityPacket(int attachedEntityId, int holdingEntityId) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<AttachEntityPacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, AttachEntityPacket::attachedEntityId,
            INT, AttachEntityPacket::holdingEntityId,
            AttachEntityPacket::new);
}
