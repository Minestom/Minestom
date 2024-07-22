package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record MapDataPacket(int mapId, byte scale, boolean locked,
                            boolean trackingPosition, @NotNull List<Icon> icons,
                            @Nullable MapDataPacket.ColorContent colorContent) implements ServerPacket.Play {
    public static final int MAX_ICONS = 1024;

    public MapDataPacket {
        icons = List.copyOf(icons);
    }

    public static final NetworkBuffer.Type<MapDataPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, MapDataPacket value) {
            writer.write(VAR_INT, value.mapId);
            writer.write(BYTE, value.scale);
            writer.write(BOOLEAN, value.locked);
            writer.write(BOOLEAN, value.trackingPosition);
            if (value.trackingPosition) writer.writeCollection(Icon.SERIALIZER, value.icons);
            if (value.colorContent != null) {
                writer.write(ColorContent.SERIALIZER, value.colorContent);
            } else {
                writer.write(BYTE, (byte) 0);
            }
        }

        @Override
        public MapDataPacket read(@NotNull NetworkBuffer reader) {
            var mapId = reader.read(VAR_INT);
            var scale = reader.read(BYTE);
            var locked = reader.read(BOOLEAN);
            var trackingPosition = reader.read(BOOLEAN);
            List<Icon> icons = trackingPosition ? reader.readCollection(Icon.SERIALIZER, MAX_ICONS) : List.of();

            var columns = reader.read(BYTE);
            if (columns <= 0) return new MapDataPacket(mapId, scale, locked, trackingPosition, icons, null);
            byte rows = reader.read(BYTE);
            byte x = reader.read(BYTE);
            byte z = reader.read(BYTE);
            byte[] data = reader.read(BYTE_ARRAY);
            return new MapDataPacket(mapId, scale, locked,
                    trackingPosition, icons, new ColorContent(columns, rows, x, z,
                    data));
        }
    };

    public record Icon(int type, byte x, byte z, byte direction,
                       @Nullable Component displayName) {
        public static final NetworkBuffer.Type<Icon> SERIALIZER = NetworkBufferTemplate.template(
                VAR_INT, Icon::type,
                BYTE, Icon::x,
                BYTE, Icon::z,
                BYTE, Icon::direction,
                COMPONENT.optional(), Icon::displayName,
                Icon::new);
    }

    public record ColorContent(byte columns, byte rows, byte x, byte z,
                               byte @NotNull [] data) {
        public static final NetworkBuffer.Type<ColorContent> SERIALIZER = NetworkBufferTemplate.template(
                BYTE, ColorContent::columns,
                BYTE, ColorContent::rows,
                BYTE, ColorContent::x,
                BYTE, ColorContent::z,
                BYTE_ARRAY, ColorContent::data,
                ColorContent::new);
    }
}
