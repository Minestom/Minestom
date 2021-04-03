package net.minestom.server.item;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ItemTag<T> {

    private final String key;
    private final Function<NBTCompound, T> readFunction;
    private final BiConsumer<NBTCompound, T> writeConsumer;

    private ItemTag(@NotNull String key,
                    @NotNull Function<NBTCompound, T> readFunction,
                    @NotNull BiConsumer<NBTCompound, T> writeConsumer) {
        this.key = key;
        this.readFunction = readFunction;
        this.writeConsumer = writeConsumer;
    }

    public @NotNull String getKey() {
        return key;
    }

    protected T read(@NotNull NBTCompound nbtCompound) {
        return readFunction.apply(nbtCompound);
    }

    protected void write(@NotNull NBTCompound nbtCompound, @NotNull T value) {
        this.writeConsumer.accept(nbtCompound, value);
    }

    public static @NotNull ItemTag<Integer> Integer(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getInt(key),
                (nbtCompound, integer) -> nbtCompound.setInt(key, integer));
    }

}
