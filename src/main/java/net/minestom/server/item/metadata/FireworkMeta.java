package net.minestom.server.item.metadata;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class FireworkMeta implements ItemMeta {

    private boolean flicker;
    private boolean trail;
    private FireworkType type;
    private int[] colors;
    private int[] fadeColors;

    private byte flightDuration;
    // TODO Explosions list

    @Override
    public boolean hasNbt() {
        return false;
    }

    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        return false;
    }

    @Override
    public void read(@NotNull NBTCompound compound) {

    }

    @Override
    public void write(@NotNull NBTCompound compound) {

    }

    @NotNull
    @Override
    public ItemMeta clone() {
        return null;
    }

    public enum FireworkType {
        SMALL_BALL, LARGE_BALL, STAR_SHAPED, CREEPER_SHAPED, BURST
    }

}
