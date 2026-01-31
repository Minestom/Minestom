package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record MapDataPacket(int mapId, byte scale, boolean locked,
                            boolean trackingPosition, List<Icon> icons,
                            @Nullable MapDataPacket.ColorContent colorContent) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final int MAX_ICONS = 1024;

    public MapDataPacket {
        icons = List.copyOf(icons);
    }

    public static final NetworkBuffer.Type<MapDataPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, MapDataPacket value) {
            buffer.write(VAR_INT, value.mapId);
            buffer.write(BYTE, value.scale);
            buffer.write(BOOLEAN, value.locked);
            buffer.write(BOOLEAN, value.trackingPosition);
            if (value.trackingPosition) buffer.write(Icon.SERIALIZER.list(), value.icons);
            if (value.colorContent != null) {
                buffer.write(ColorContent.SERIALIZER, value.colorContent);
            } else {
                buffer.write(BYTE, (byte) 0x00);
            }
        }

        @Override
        public MapDataPacket read(NetworkBuffer buffer) {
            int mapId = buffer.read(VAR_INT);
            byte scale = buffer.read(BYTE);
            boolean locked = buffer.read(BOOLEAN);
            boolean trackingPosition = buffer.read(BOOLEAN);
            List<Icon> icons = trackingPosition ? buffer.read(Icon.SERIALIZER.list(MAX_ICONS)) : List.of();

            byte columns = buffer.read(BYTE);
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

    @Override
    public @Unmodifiable Collection<Component> components() {
        if (this.icons.isEmpty()) return List.of();
        return this.icons.stream().map(Icon::components).flatMap(Collection::stream).toList();
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        if (this.icons.isEmpty()) return this;
        return new MapDataPacket(
                this.mapId,
                this.scale,
                this.locked,
                this.trackingPosition,
                this.icons.stream().map(it -> it.copyWithOperator(operator)).toList(),
                this.colorContent
        );
    }

    public record Icon(int type, byte x, byte z, byte direction,
                       @Nullable Component displayName) implements ComponentHolder<Icon> {
        public static final NetworkBuffer.Type<Icon> SERIALIZER = NetworkBufferTemplate.template(
                VAR_INT, Icon::type,
                BYTE, Icon::x,
                BYTE, Icon::z,
                BYTE, Icon::direction,
                COMPONENT.optional(), Icon::displayName,
                Icon::new);

        @Override
        public Collection<Component> components() {
            if (displayName != null) return List.of(displayName);
            return List.of();
        }

        @Override
        public Icon copyWithOperator(UnaryOperator<Component> operator) {
            if (displayName != null) return new Icon(type, x, z, direction, operator.apply(displayName));
            return this;
        }
    }

    public record ColorContent(byte columns, byte rows, byte x, byte z,
                               byte[] data) {
        public static final NetworkBuffer.Type<ColorContent> SERIALIZER = NetworkBufferTemplate.template(
                BYTE, ColorContent::columns,
                BYTE, ColorContent::rows,
                BYTE, ColorContent::x,
                BYTE, ColorContent::z,
                BYTE_ARRAY, ColorContent::data,
                ColorContent::new);

        public ColorContent {
            data = data.clone();
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof ColorContent(byte columns1, byte rows1, byte x1, byte z1, byte[] data1))) return false;
            return x() == x1 && z() == z1 && rows() == rows1 && columns() == columns1 && Arrays.equals(data(), data1);
        }

        @Override
        public int hashCode() {
            int result = columns();
            result = 31 * result + rows();
            result = 31 * result + x();
            result = 31 * result + z();
            result = 31 * result + Arrays.hashCode(data());
            return result;
        }
    }
}
