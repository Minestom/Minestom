package net.minestom.server.item.metadata;

import net.minestom.server.color.Color;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.tag.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public record MapMeta(TagReadable readable) implements ItemMetaView<MapMeta.Builder> {
    private static final Tag<Integer> MAP_ID = Tag.Integer("map").defaultValue(0);
    private static final Tag<Integer> MAP_SCALE_DIRECTION = Tag.Integer("map_scale_direction").defaultValue(0);
    private static final Tag<List<Decoration>> DECORATIONS = Tag.Structure("Decorations", new TagSerializer<Decoration>() {
        @Override
        public @Nullable Decoration read(@NotNull TagReadable reader) {
            final String id = reader.getTag(Tag.String("id"));
            final Byte type = reader.getTag(Tag.Byte("type"));
            final Byte x = reader.getTag(Tag.Byte("x"));
            final Byte z = reader.getTag(Tag.Byte("z"));
            final Double rot = reader.getTag(Tag.Double("rot"));
            if (id == null || type == null || x == null || z == null || rot == null) return null;
            return new Decoration(id, type, x, z, rot);
        }

        @Override
        public void write(@NotNull TagWritable writer, @NotNull Decoration value) {
            writer.setTag(Tag.String("id"), value.id);
            writer.setTag(Tag.Byte("type"), value.type);
            writer.setTag(Tag.Byte("x"), value.x);
            writer.setTag(Tag.Byte("z"), value.z);
            writer.setTag(Tag.Double("rot"), value.rotation);
        }
    }).list().defaultValue(List.of());
    private static final Tag<Color> MAP_COLOR = Tag.Integer("MapColor").path("display").map(Color::new, Color::asRGB);

    public int getMapId() {
        return getTag(MAP_ID);
    }

    public int getMapScaleDirection() {
        return getTag(MAP_SCALE_DIRECTION);
    }

    public List<Decoration> getDecorations() {
        return getTag(DECORATIONS);
    }

    public @NotNull Color getMapColor() {
        return getTag(MAP_COLOR);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder mapId(int value) {
            setTag(MAP_ID, value);
            return this;
        }

        public Builder mapScaleDirection(int value) {
            setTag(MAP_SCALE_DIRECTION, value);
            return this;
        }

        public Builder decorations(List<Decoration> value) {
            setTag(DECORATIONS, value);
            return this;
        }

        public Builder mapColor(Color value) {
            setTag(MAP_COLOR, value);
            return this;
        }
    }

    public record Decoration(String id, byte type, byte x, byte z, double rotation) {
    }
}
