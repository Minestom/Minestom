package net.minestom.server.item.metadata;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.tag.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public record CompassMeta(TagReadable readable) implements ItemMetaView<CompassMeta.Builder> {
    private static final Tag<Boolean> LODESTONE_TRACKED = Tag.Boolean("LodestoneTracked").defaultValue(false);
    private static final Tag<String> LODESTONE_DIMENSION = Tag.String("LodestoneDimension");
    private static final Tag<Point> LODESTONE_POSITION = Tag.Structure("LodestonePos", new TagSerializer<>() {
        @Override
        public @Nullable Point read(@NotNull TagReadable reader) {
            final Integer x = reader.getTag(Tag.Integer("X"));
            final Integer y = reader.getTag(Tag.Integer("Y"));
            final Integer z = reader.getTag(Tag.Integer("Z"));
            if (x == null || y == null || z == null) return null;
            return new Vec(x, y, z);
        }

        @Override
        public void write(@NotNull TagWritable writer, @NotNull Point value) {
            writer.setTag(Tag.Integer("X"), value.blockX());
            writer.setTag(Tag.Integer("Y"), value.blockY());
            writer.setTag(Tag.Integer("Z"), value.blockZ());
        }
    });

    public boolean isLodestoneTracked() {
        return getTag(LODESTONE_TRACKED);
    }

    public @Nullable String getLodestoneDimension() {
        return getTag(LODESTONE_DIMENSION);
    }

    public @Nullable Point getLodestonePosition() {
        return getTag(LODESTONE_POSITION);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public Builder lodestoneTracked(boolean lodestoneTracked) {
            setTag(LODESTONE_TRACKED, lodestoneTracked);
            return this;
        }

        public Builder lodestoneDimension(@Nullable String lodestoneDimension) {
            setTag(LODESTONE_DIMENSION, lodestoneDimension);
            return this;
        }

        public Builder lodestonePosition(@Nullable Point lodestonePosition) {
            setTag(LODESTONE_POSITION, lodestonePosition);
            return this;
        }
    }
}
