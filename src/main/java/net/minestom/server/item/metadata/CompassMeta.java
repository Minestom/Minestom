package net.minestom.server.item.metadata;

import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public record CompassMeta(TagReadable readable) implements ItemMetaView {
    private static final Tag<Byte> LODESTONE_TRACKED = Tag.Byte("LodestoneTracked");
    private static final Tag<String> LODESTONE_DIMENSION = Tag.String("LodestoneDimension");
    private static final Tag<Point> LODESTONE_POSITION = null;

    public boolean isLodestoneTracked() {
        return getTag(LODESTONE_TRACKED) == 1;
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
            setTag(LODESTONE_TRACKED, lodestoneTracked ? (byte) 1 : (byte) 0);
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
