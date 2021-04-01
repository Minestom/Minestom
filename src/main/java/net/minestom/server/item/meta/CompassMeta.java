package net.minestom.server.item.meta;

import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public class CompassMeta extends ItemMeta {

    private final boolean lodestoneTracked;
    private final String lodestoneDimension;
    private final Position lodestonePosition;

    protected CompassMeta(ItemMetaBuilder metaBuilder,
                          boolean lodestoneTracked, String lodestoneDimension, Position lodestonePosition) {
        super(metaBuilder);
        this.lodestoneTracked = lodestoneTracked;
        this.lodestoneDimension = lodestoneDimension;
        this.lodestonePosition = lodestonePosition;
    }

    public boolean isLodestoneTracked() {
        return lodestoneTracked;
    }

    public String getLodestoneDimension() {
        return lodestoneDimension;
    }

    public Position getLodestonePosition() {
        return lodestonePosition;
    }

    public static class Builder extends ItemMetaBuilder {

        private boolean lodestoneTracked;
        private String lodestoneDimension;
        private Position lodestonePosition;

        public Builder lodestoneTracked(boolean lodestoneTracked) {
            this.lodestoneTracked = lodestoneTracked;
            return this;
        }

        public Builder lodestoneDimension(String lodestoneDimension) {
            this.lodestoneDimension = lodestoneDimension;
            return this;
        }

        public Builder lodestonePosition(Position lodestonePosition) {
            this.lodestonePosition = lodestonePosition;
            return this;
        }

        @Override
        public @NotNull CompassMeta build() {
            return new CompassMeta(this, lodestoneTracked, lodestoneDimension, lodestonePosition);
        }

        @Override
        protected void deepClone(@NotNull ItemMetaBuilder metaBuilder) {
            var compassBuilder = (CompassMeta.Builder) metaBuilder;
            compassBuilder.lodestoneTracked = lodestoneTracked;
            compassBuilder.lodestoneDimension = lodestoneDimension;
            compassBuilder.lodestonePosition = lodestonePosition;
        }
    }
}
