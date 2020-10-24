package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class MapDataPacket implements ServerPacket {

    public int mapId;
    public byte scale;
    public boolean trackingPosition;
    public boolean locked;

    public Icon[] icons;

    public short columns;
    public short rows;
    public byte x;
    public byte z;
    public byte[] data;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(mapId);
        writer.writeByte(scale);
        writer.writeBoolean(trackingPosition);
        writer.writeBoolean(locked);

        if (icons != null && icons.length > 0) {
            writer.writeVarInt(icons.length);
            for (Icon icon : icons) {
                icon.write(writer);
            }
        } else {
            writer.writeVarInt(0);
        }

        writer.writeByte((byte) columns);
        if (columns <= 0) {
            return;
        }

        writer.writeByte((byte) rows);
        writer.writeByte(x);
        writer.writeByte(z);
        if (data != null && data.length > 0) {
            writer.writeVarInt(data.length);
            writer.writeBytes(data);
        } else {
            writer.writeVarInt(0);
        }

    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.MAP_DATA;
    }

    public static class Icon {
        public int type;
        public byte x, z;
        public byte direction;
        public JsonMessage displayName; // Only text

        private void write(BinaryWriter writer) {
            writer.writeVarInt(type);
            writer.writeByte(x);
            writer.writeByte(z);
            writer.writeByte(direction);

            final boolean hasDisplayName = displayName != null;
            writer.writeBoolean(hasDisplayName);
            if (hasDisplayName) {
                writer.writeSizedString(displayName.toString());
            }
        }

    }

}
