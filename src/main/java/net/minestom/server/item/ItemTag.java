package net.minestom.server.item;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public abstract class ItemTag<T> {

    private final String key;

    private ItemTag(@NotNull String key) {
        this.key = key;
    }

    public @NotNull String getKey() {
        return key;
    }

    protected abstract T read(@NotNull NBTCompound nbtCompound);

    protected abstract void write(@NotNull NBTCompound nbtCompound, @NotNull T value);

    public static @NotNull ItemTag<Integer> Integer(@NotNull String key) {
        return new ItemTag<>(key) {
            @Override
            protected Integer read(@NotNull NBTCompound nbtCompound) {
                return nbtCompound.getInt(key);
            }

            @Override
            protected void write(@NotNull NBTCompound nbtCompound, @NotNull Integer value) {
                nbtCompound.setInt(key, value);
            }
        };
    }

}
