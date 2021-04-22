package net.minestom.server.network.packet.server.play;

import net.minestom.server.attribute.*;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class EntityPropertiesPacket implements ServerPacket {

    public int entityId;
    public Property[] properties = new Property[0];

    public EntityPropertiesPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeInt(properties.length);
        for (Property property : properties) {
            property.write(writer);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        int propertyCount = reader.readInt();
        properties = new Property[propertyCount];
        for (int i = 0; i < propertyCount; i++) {
            properties[i] = new Property();
            properties[i].read(reader);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_PROPERTIES;
    }

    public static class Property implements Writeable, Readable {

        public Attribute attribute;
        public double value;
        public AttributeInstance instance;

        public void write(BinaryWriter writer) {
            double v = value;
            if (attribute instanceof ClampedAttribute) {
                double maxValue = ((ClampedAttribute) attribute).getMaxValue();
                double minValue = ((ClampedAttribute) attribute).getMinValue();
                // Bypass vanilla limit client-side if needed (by sending the max/min value allowed)
                v = Math.max(Math.min(value, maxValue), minValue);
            }


            writer.writeSizedString(attribute.getId().getDomain());
            writer.writeDouble(v);

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

        @Override
        public void read(@NotNull BinaryReader reader) {
            String key = reader.readSizedString(Integer.MAX_VALUE);
            attribute = Registry.ATTRIBUTE_REGISTRY.get(key); //TODO: Static

            value = reader.readDouble();

            int modifierCount = reader.readVarInt();
            instance = new AttributeInstance(attribute, null);
            for (int i = 0; i < modifierCount; i++) {
                AttributeModifier modifier = new AttributeModifier(reader.readUuid(), "", (float) reader.readDouble(), AttributeOperation.fromId(reader.readByte()));
                instance.addModifier(modifier);
            }
        }
    }

}
