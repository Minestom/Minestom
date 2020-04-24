package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class EntityPropertiesPacket implements ServerPacket {

    public int entityId;
    public Property[] properties;


    @Override
    public void write(PacketWriter writer) {
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

        public String key;
        public double value;

        private void write(PacketWriter writer) {
            writer.writeSizedString(key);
            writer.writeDouble(value);

            // TODO modifiers
            writer.writeVarInt(0);
        }
    }

}
