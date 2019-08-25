package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class EntityPropertiesPacket implements ServerPacket {

    public int entityId;
    public Property[] properties;


    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putInt(properties.length);
        for (Property property : properties) {
            property.write(buffer);
        }
    }

    @Override
    public int getId() {
        return 0x58;
    }

    public static class Property {

        public String key;
        public double value;

        private void write(Buffer buffer) {
            Utils.writeString(buffer, key);
            buffer.putDouble(value);

            // TODO Modifiers
            Utils.writeVarInt(buffer, 0);
        }
    }

}
