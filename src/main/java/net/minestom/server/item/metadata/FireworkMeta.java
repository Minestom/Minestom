package net.minestom.server.item.metadata;

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
    public boolean isSimilar(ItemMeta itemMeta) {
        return false;
    }

    @Override
    public void read(NBTCompound compound) {

    }

    @Override
    public void write(NBTCompound compound) {

    }

    @Override
    public ItemMeta clone() {
        return null;
    }

    public enum FireworkType {
        SMALL_BALL, LARGE_BALL, STAR_SHAPED, CREEPER_SHAPED, BURST
    }

}
