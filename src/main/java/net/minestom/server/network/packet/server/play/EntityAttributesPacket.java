package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityAttributesPacket(int entityId, List<AttributeInstance> properties) implements ServerPacket.Play {
    public static final int MAX_ENTRIES = 1024;

    public EntityAttributesPacket {
        properties = List.copyOf(properties);
    }

    public EntityAttributesPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.readCollection(r -> {
            int id = reader.read(VAR_INT);
            final Attribute attribute = Attribute.fromId(id);
            Check.notNull(attribute, "Unknown attribute id: " + id);

            final double value = reader.read(DOUBLE);
            int modifierCount = reader.read(VAR_INT);
            AttributeInstance instance = new AttributeInstance(attribute, null);
            for (int i = 0; i < modifierCount; i++) {
                AttributeModifier modifier = new AttributeModifier(reader.read(UUID), "", reader.read(DOUBLE), AttributeOperation.fromId(reader.read(BYTE)));
                instance.addModifier(modifier);
            }
            return instance;
        }, MAX_ENTRIES));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(VAR_INT, properties.size());
        for (AttributeInstance instance : properties) {
            final Attribute attribute = instance.getAttribute();

            writer.write(VAR_INT, attribute.id());
            writer.write(DOUBLE, instance.getBaseValue());

            {
                Collection<AttributeModifier> modifiers = instance.getModifiers();
                writer.write(VAR_INT, modifiers.size());

                for (var modifier : modifiers) {
                    writer.write(UUID, modifier.getId());
                    writer.write(DOUBLE, modifier.getAmount());
                    writer.write(BYTE, (byte) modifier.getOperation().getId());
                }
            }
        }
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.ENTITY_ATTRIBUTES;
    }
}
