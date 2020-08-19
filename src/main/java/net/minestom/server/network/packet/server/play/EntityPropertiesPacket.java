package net.minestom.server.network.packet.server.play;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class EntityPropertiesPacket implements ServerPacket {

    public int entityId;
    public Property[] properties;


    @Override
    public void write(BinaryWriter writer) {
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

        private void write(BinaryWriter writer) {
            float maxValue = attribute.getMaxVanillaValue();

            // Bypass vanilla limit client-side if needed (by sending the max value allowed)
            final double v = value > maxValue ? maxValue : value;

            writer.writeSizedString(attribute.getKey());
            writer.writeDouble(v);

            // TODO support for AttributeOperation
            writer.writeVarInt(0);
        }
    }

}
