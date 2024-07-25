package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityAttributesPacket(int entityId, List<AttributeInstance> attributes) implements ServerPacket.Play {
    public static final int MAX_ENTRIES = 1024;

    public static final NetworkBuffer.Type<EntityAttributesPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityAttributesPacket::entityId,
            AttributeInstance.NETWORK_TYPE.list(MAX_ENTRIES), EntityAttributesPacket::attributes,
            EntityAttributesPacket::new);

    public EntityAttributesPacket {
        attributes = List.copyOf(attributes);
    }
}
