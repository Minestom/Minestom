package net.minestom.server.item.metadata;

import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Objects;

public class CompassMeta extends ItemMeta {

    private boolean lodestoneTracked;
    private String lodestoneDimension;

    private Position lodestonePosition;

    public boolean isLodestoneTracked() {
        return lodestoneTracked;
    }

    public void setLodestoneTracked(boolean lodestoneTracked) {
        this.lodestoneTracked = lodestoneTracked;
    }

    @Nullable
    public String getLodestoneDimension() {
        return lodestoneDimension;
    }

    public void setLodestoneDimension(@Nullable String lodestoneDimension) {
        this.lodestoneDimension = lodestoneDimension;
    }

    @Nullable
    public Position getLodestonePosition() {
        return lodestonePosition;
    }

    public void setLodestonePosition(@Nullable Position lodestonePosition) {
        this.lodestonePosition = lodestonePosition;
    }

    @Override
    public boolean hasNbt() {
        return true;
    }

    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        if (!(itemMeta instanceof CompassMeta))
            return false;
        CompassMeta compassMeta = (CompassMeta) itemMeta;
        return (compassMeta.lodestoneTracked == lodestoneTracked) &&
                (Objects.equals(compassMeta.lodestoneDimension, lodestoneDimension)) &&
                (Objects.equals(compassMeta.lodestonePosition, lodestonePosition));
    }

    @Override
    public void read(@NotNull NBTCompound compound) {
        if (compound.containsKey("LodestoneTracked")) {
            this.lodestoneTracked = compound.getByte("LodestoneTracked") == 1;
        }
        if (compound.containsKey("LodestoneDimension")) {
            this.lodestoneDimension = compound.getString("LodestoneDimension");
        }
        if (compound.containsKey("LodestonePos")) {
            final NBTCompound posCompound = compound.getCompound("LodestonePos");
            final int x = posCompound.getInt("X");
            final int y = posCompound.getInt("Y");
            final int z = posCompound.getInt("Z");

            this.lodestonePosition = new Position(x, y, z);
        }
    }

    @Override
    public void write(@NotNull NBTCompound compound) {
        compound.setByte("LodestoneTracked", (byte) (lodestoneTracked ? 1 : 0));
        if (lodestoneDimension != null) {
            compound.setString("LodestoneDimension", lodestoneDimension);
        }

        if (lodestonePosition != null) {
            NBTCompound posCompound = new NBTCompound();
            posCompound.setInt("X", (int) lodestonePosition.getX());
            posCompound.setInt("Y", (int) lodestonePosition.getY());
            posCompound.setInt("Z", (int) lodestonePosition.getZ());
            compound.set("LodestonePos", posCompound);
        }
    }

    @NotNull
    @Override
    public ItemMeta clone() {
        CompassMeta compassMeta = (CompassMeta) super.clone();
        compassMeta.lodestoneTracked = lodestoneTracked;
        compassMeta.lodestoneDimension = lodestoneDimension;
        compassMeta.lodestonePosition = lodestonePosition != null ? lodestonePosition.clone() : null;

        return compassMeta;
    }
}
