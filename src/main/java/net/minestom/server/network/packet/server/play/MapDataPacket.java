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
                            byte columns, byte rows, byte x, byte z,
                            byte @Nullable [] data) implements ServerPacket {
    public MapDataPacket {
        icons = List.copyOf(icons);
    }

    public MapDataPacket(BinaryReader reader) {
        this(read(reader));
    }

    private MapDataPacket(MapDataPacket packet) {
        this(packet.mapId, packet.scale, packet.locked,
                packet.trackingPosition, packet.icons,
                packet.columns, packet.rows, packet.x, packet.z,
                packet.data);
    }

    private static MapDataPacket read(BinaryReader reader) {
        var mapId = reader.readVarInt();
        var scale = reader.readByte();
        var locked = reader.readBoolean();
        var trackingPosition = reader.readBoolean();
        List<Icon> icons = trackingPosition ? reader.readVarIntList(Icon::new) : List.of();

        var columns = reader.readByte();
        byte rows = 0;
        byte x = 0;
        byte z = 0;
        byte[] data = null;
        if (columns > 0) {
            rows = reader.readByte();
            x = reader.readByte();
            z = reader.readByte();
            data = reader.readBytes(reader.readVarInt());
        }
        return new MapDataPacket(mapId, scale, locked,
                trackingPosition, icons, columns, rows, x, z,
                data);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(mapId);
        writer.writeByte(scale);
        writer.writeBoolean(locked);
        writer.writeBoolean(trackingPosition);
        if (trackingPosition) writer.writeVarIntList(icons, BinaryWriter::write);

        writer.writeByte(columns);
        if (columns <= 0) return;
        writer.writeByte(rows);
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
}
