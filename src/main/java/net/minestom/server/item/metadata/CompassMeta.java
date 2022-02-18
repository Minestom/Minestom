package net.minestom.server.item.metadata;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTByte;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTString;

import java.util.Map;

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
            mutableNbt().setByte("LodestoneTracked", (byte) (lodestoneTracked ? 1 : 0));
            return this;
        }

        public Builder lodestoneDimension(@Nullable String lodestoneDimension) {
            this.lodestoneDimension = lodestoneDimension;
            if (lodestoneDimension != null) {
                mutableNbt().setString("LodestoneDimension", lodestoneDimension);
            } else {
                mutableNbt().remove("LodestoneDimension");
            }
            return this;
        }

        public Builder lodestonePosition(@Nullable Point lodestonePosition) {
            this.lodestonePosition = lodestonePosition;
            if (lodestonePosition != null) {
                mutableNbt().set("LodestonePos", NBT.Compound(Map.of(
                        "X", NBT.Int(lodestonePosition.blockX()),
                        "Y", NBT.Int(lodestonePosition.blockY()),
                        "Z", NBT.Int(lodestonePosition.blockZ()))));
            } else {
                mutableNbt().remove("LodestonePos");
            }
            return this;
        }

        @Override
        public @NotNull CompassMeta build() {
            return new CompassMeta(this, lodestoneTracked, lodestoneDimension, lodestonePosition);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.get("LodestoneTracked") instanceof NBTByte tracked) {
                this.lodestoneTracked = tracked.asBoolean();
            }
            if (nbtCompound.get("LodestoneDimension") instanceof NBTString dimension) {
                this.lodestoneDimension = dimension.getValue();
            }
            if (nbtCompound.get("LodestonePos") instanceof NBTCompound posCompound) {
                final int x = posCompound.getInt("X");
                final int y = posCompound.getInt("Y");
                final int z = posCompound.getInt("Z");
                this.lodestonePosition = new Vec(x, y, z);
            }
        }
    }
}
