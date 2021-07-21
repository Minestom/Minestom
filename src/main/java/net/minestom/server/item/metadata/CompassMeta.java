package net.minestom.server.item.metadata;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.Supplier;

public class CompassMeta extends ItemMeta implements ItemMetaBuilder.Provider<CompassMeta.Builder> {

    private final boolean lodestoneTracked;
    private final String lodestoneDimension;
    private final Point lodestonePosition;

    protected CompassMeta(ItemMetaBuilder metaBuilder,
                          boolean lodestoneTracked,
                          @Nullable String lodestoneDimension,
                          @Nullable Point lodestonePosition) {
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

    public @Nullable Point getLodestonePosition() {
        return lodestonePosition;
    }

    public static class Builder extends ItemMetaBuilder {

        private boolean lodestoneTracked;
        private String lodestoneDimension;
        private Point lodestonePosition;

        public Builder lodestoneTracked(boolean lodestoneTracked) {
            this.lodestoneTracked = lodestoneTracked;
            mutateNbt(compound -> compound.setByte("LodestoneTracked", (byte) (lodestoneTracked ? 1 : 0)));
            return this;
        }

        public Builder lodestoneDimension(@Nullable String lodestoneDimension) {
            this.lodestoneDimension = lodestoneDimension;

            mutateNbt(compound -> {
                if (lodestoneDimension != null) {
                    compound.setString("LodestoneDimension", lodestoneDimension);
                } else {
                    compound.removeTag("LodestoneDimension");
                }
            });

            return this;
        }

        public Builder lodestonePosition(@Nullable Point lodestonePosition) {
            this.lodestonePosition = lodestonePosition;

            mutateNbt(compound -> {
                if (lodestonePosition != null) {
                    NBTCompound posCompound = new NBTCompound();
                    posCompound.setInt("X", lodestonePosition.blockX());
                    posCompound.setInt("Y", lodestonePosition.blockY());
                    posCompound.setInt("Z", lodestonePosition.blockZ());
                    compound.set("LodestonePos", posCompound);
                } else {
                    compound.removeTag("LodestonePos");
                }
            });

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
                lodestonePosition(new Vec(x, y, z));
            }
        }

        @Override
        protected @NotNull Supplier<ItemMetaBuilder> getSupplier() {
            return Builder::new;
        }
    }
}
