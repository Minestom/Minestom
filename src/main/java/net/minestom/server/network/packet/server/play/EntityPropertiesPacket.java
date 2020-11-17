package net.minestom.server.network.packet.server.play;

import java.util.Collection;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityPropertiesPacket implements ServerPacket {

    public int entityId;
    public Property[] properties;


    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeInt(properties.length);
        for (Property property : properties) {
            property.write(writer);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_PROPERTIES;
    }

    public static class Property {

        public Attribute attribute;
        public double value;
        public AttributeInstance instance;

        private void write(BinaryWriter writer) {
            if (instance != null) {
                attribute = instance.getAttribute();
                value = instance.getBaseValue();
            }
            float maxValue = attribute.getMaxValue();

            // Bypass vanilla limit client-side if needed (by sending the max value allowed)
            final double v = value > maxValue ? maxValue : value;

            writer.writeSizedString(attribute.getKey());
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
    }

}
