package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.DOUBLE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityAttributesPacket(int entityId, List<Property> properties) implements ServerPacket.Play {
    public static final int MAX_ENTRIES = 1024;

    public static final NetworkBuffer.Type<EntityAttributesPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityAttributesPacket::entityId,
            Property.NETWORK_TYPE.list(MAX_ENTRIES), EntityAttributesPacket::properties,
            EntityAttributesPacket::new);

    public EntityAttributesPacket {
        properties = List.copyOf(properties);
    }

    public record Property(Attribute attribute, double value, Collection<AttributeModifier> modifiers) {
        public static final NetworkBuffer.Type<Property> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Property value) {
                buffer.write(Attribute.NETWORK_TYPE, value.attribute);
                buffer.write(DOUBLE, value.value);
                buffer.writeCollection(AttributeModifier.NETWORK_TYPE, value.modifiers);
            }

            @Override
            public Property read(@NotNull NetworkBuffer buffer) {
                return new Property(buffer.read(Attribute.NETWORK_TYPE), buffer.read(DOUBLE), buffer.readCollection(AttributeModifier.NETWORK_TYPE, Short.MAX_VALUE));
            }
        };

        public Property {
            modifiers = List.copyOf(modifiers);
        }
    }
}
