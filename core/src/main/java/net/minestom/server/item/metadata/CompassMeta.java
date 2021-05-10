package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.Supplier;

public class CompassMeta extends ItemMeta implements ItemMetaBuilder.Provider<CompassMeta.Builder> {

    private final boolean lodestoneTracked;
    private final String lodestoneDimension;
    private final Position lodestonePosition;

    protected CompassMeta(ItemMetaBuilder metaBuilder,
                          boolean lodestoneTracked,
                          @Nullable String lodestoneDimension,
                          @Nullable Position lodestonePosition) {
        super(metaBuilder);
        this.lodestoneTracked = lodestoneTracked;
        this.lodestoneDimension = lodestoneDimension;
        this.lodestonePosition = lodestonePosition;
    }

    public boolean isLodestoneTracked() {
        return lodestoneTracked;
    }

    public @Nullable String getLodestoneDimension() {
        return lodestoneDimension;
    }

    public @Nullable Position getLodestonePosition() {
        return lodestonePosition;
    }

    public static class Builder extends ItemMetaBuilder {

        private boolean lodestoneTracked;
        private String lodestoneDimension;
        private Position lodestonePosition;

        public Builder lodestoneTracked(boolean lodestoneTracked) {
            this.lodestoneTracked = lodestoneTracked;
            this.nbt.setByte("LodestoneTracked", (byte) (lodestoneTracked ? 1 : 0));
            return this;
        }

        public Builder lodestoneDimension(@Nullable String lodestoneDimension) {
            this.lodestoneDimension = lodestoneDimension;

            if (lodestoneDimension != null) {
                this.nbt.setString("LodestoneDimension", lodestoneDimension);
            } else {
                this.nbt.removeTag("LodestoneDimension");
            }

            return this;
        }

        public Builder lodestonePosition(@Nullable Position lodestonePosition) {
            this.lodestonePosition = lodestonePosition;

            if (lodestonePosition != null) {
                NBTCompound posCompound = new NBTCompound();
                posCompound.setInt("X", (int) lodestonePosition.getX());
                posCompound.setInt("Y", (int) lodestonePosition.getY());
                posCompound.setInt("Z", (int) lodestonePosition.getZ());
                this.nbt.set("LodestonePos", posCompound);
            } else {
                this.nbt.removeTag("LodestonePos");
            }

            return this;
        }

        @Override
        public @NotNull CompassMeta build() {
            return new CompassMeta(this, lodestoneTracked, lodestoneDimension, lodestonePosition);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.containsKey("LodestoneTracked")) {
                lodestoneTracked(nbtCompound.getByte("LodestoneTracked") == 1);
            }
            if (nbtCompound.containsKey("LodestoneDimension")) {
                lodestoneDimension(nbtCompound.getString("LodestoneDimension"));
            }
            if (nbtCompound.containsKey("LodestonePos")) {
                final NBTCompound posCompound = nbtCompound.getCompound("LodestonePos");
                final int x = posCompound.getInt("X");
                final int y = posCompound.getInt("Y");
                final int z = posCompound.getInt("Z");
                lodestonePosition(new Position(x, y, z));
            }
        }

        @Override
        protected @NotNull Supplier<ItemMetaBuilder> getSupplier() {
            return Builder::new;
        }
    }
}
