package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record MapDataPacket(int mapId, byte scale, boolean locked,
                            boolean trackingPosition, @NotNull List<Icon> icons,
                            @Nullable MapDataPacket.ColorContent colorContent) implements ServerPacket {
    public MapDataPacket {
        icons = List.copyOf(icons);
    }

    public MapDataPacket(BinaryReader reader) {
        this(read(reader));
    }

    private MapDataPacket(MapDataPacket packet) {
        this(packet.mapId, packet.scale, packet.locked,
                packet.trackingPosition, packet.icons,
                packet.colorContent);
    }

    private static MapDataPacket read(BinaryReader reader) {
        var mapId = reader.readVarInt();
        var scale = reader.readByte();
        var locked = reader.readBoolean();
        var trackingPosition = reader.readBoolean();
        List<Icon> icons = trackingPosition ? reader.readVarIntList(Icon::new) : List.of();

        var columns = reader.readByte();
        if (columns <= 0) return new MapDataPacket(mapId, scale, locked, trackingPosition, icons, null);
        byte rows = reader.readByte();
        byte x = reader.readByte();
        byte z = reader.readByte();
        byte[] data = reader.readByteArray();
        return new MapDataPacket(mapId, scale, locked,
                trackingPosition, icons, new ColorContent(columns, rows, x, z,
                data));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(mapId);
        writer.writeByte(scale);
        writer.writeBoolean(locked);
        writer.writeBoolean(trackingPosition);
        if (trackingPosition) writer.writeVarIntList(icons, BinaryWriter::write);
        if (colorContent != null) {
            writer.write(colorContent);
        } else {
            writer.writeByte((byte) 0);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.MAP_DATA;
    }

    public record Icon(int type, byte x, byte z, byte direction,
                       @Nullable Component displayName) implements Writeable {
        public Icon(BinaryReader reader) {
            this(reader.readVarInt(), reader.readByte(), reader.readByte(), reader.readByte(),
                    reader.readBoolean() ? reader.readComponent() : null);
        }

        public void write(BinaryWriter writer) {
            writer.writeVarInt(type);
            writer.writeByte(x);
            writer.writeByte(z);
            writer.writeByte(direction);
            writer.writeBoolean(displayName != null);
            if (displayName != null) writer.writeComponent(displayName);
        }
    }

    public record ColorContent(byte columns, byte rows, byte x, byte z,
                               byte @NotNull [] data) implements Writeable {
        public ColorContent(BinaryReader reader) {
            this(reader.readByte(), reader.readByte(), reader.readByte(), reader.readByte(),
                    reader.readByteArray());
        }

        public void write(BinaryWriter writer) {
            writer.writeByte(columns);
            writer.writeByte(rows);
            writer.writeByte(x);
            writer.writeByte(z);
            writer.writeByteArray(data);
        }
    }
}
