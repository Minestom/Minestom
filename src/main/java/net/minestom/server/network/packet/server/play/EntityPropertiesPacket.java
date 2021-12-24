package net.minestom.server.network.packet.server.play;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public record EntityPropertiesPacket(int entityId, List<AttributeInstance> properties) implements ServerPacket {
    public EntityPropertiesPacket {
        properties = List.copyOf(properties);
    }

    public EntityPropertiesPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readVarIntList(r -> {
            final Attribute attribute = Attribute.fromKey(reader.readSizedString());
            final double value = reader.readDouble();
            int modifierCount = reader.readVarInt();
            AttributeInstance instance = new AttributeInstance(attribute, null);
            for (int i = 0; i < modifierCount; i++) {
                AttributeModifier modifier = new AttributeModifier(reader.readUuid(), "", (float) reader.readDouble(), AttributeOperation.fromId(reader.readByte()));
                instance.addModifier(modifier);
            }
            return instance;
        }));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeVarInt(properties.size());
        for (AttributeInstance instance : properties) {
            final Attribute attribute = instance.getAttribute();

            writer.writeSizedString(attribute.key());
            writer.writeDouble(instance.getBaseValue());

            {
                Collection<AttributeModifier> modifiers = instance.getModifiers();
                writer.writeVarInt(modifiers.size());

                for (var modifier : modifiers) {
                    writer.writeUuid(modifier.getId());
                    writer.writeDouble(modifier.getAmount());
                    writer.writeByte((byte) modifier.getOperation().getId());
                }
            }
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_PROPERTIES;
    }
}
