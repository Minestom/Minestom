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
        public void write(@NotNull NetworkBuffer buffer, MapDataPacket value) {
            buffer.write(VAR_INT, value.mapId);
            buffer.write(BYTE, value.scale);
            buffer.write(BOOLEAN, value.locked);
            buffer.write(BOOLEAN, value.trackingPosition);
            if (value.trackingPosition) buffer.writeCollection(Icon.SERIALIZER, value.icons);
            if (value.colorContent != null) {
                buffer.write(ColorContent.SERIALIZER, value.colorContent);
            } else {
                buffer.write(BYTE, (byte) 0);
            }
        }

        @Override
        public MapDataPacket read(@NotNull NetworkBuffer buffer) {
            var mapId = buffer.read(VAR_INT);
            var scale = buffer.read(BYTE);
            var locked = buffer.read(BOOLEAN);
            var trackingPosition = buffer.read(BOOLEAN);
            List<Icon> icons = trackingPosition ? buffer.readCollection(Icon.SERIALIZER, MAX_ICONS) : List.of();

            var columns = buffer.read(BYTE);
            if (columns <= 0) return new MapDataPacket(mapId, scale, locked, trackingPosition, icons, null);
            byte rows = buffer.read(BYTE);
            byte x = buffer.read(BYTE);
            byte z = buffer.read(BYTE);
            byte[] data = buffer.read(BYTE_ARRAY);
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
