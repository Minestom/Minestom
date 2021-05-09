package net.minestom.server.item;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

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

    public static @NotNull ItemTag<Byte> Byte(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getByte(key),
                (nbtCompound, value) -> nbtCompound.setByte(key, value));
    }

    public static @NotNull ItemTag<Short> Short(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getShort(key),
                (nbtCompound, value) -> nbtCompound.setShort(key, value));
    }

    public static @NotNull ItemTag<Integer> Integer(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getInt(key),
                (nbtCompound, integer) -> nbtCompound.setInt(key, integer));
    }

    public static @NotNull ItemTag<Long> Long(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getLong(key),
                (nbtCompound, value) -> nbtCompound.setLong(key, value));
    }

    public static @NotNull ItemTag<Float> Float(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getFloat(key),
                (nbtCompound, value) -> nbtCompound.setFloat(key, value));
    }

    public static @NotNull ItemTag<Double> Double(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getDouble(key),
                (nbtCompound, value) -> nbtCompound.setDouble(key, value));
    }

    public static @NotNull ItemTag<byte[]> ByteArray(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getByteArray(key),
                (nbtCompound, value) -> nbtCompound.setByteArray(key, value));
    }

    public static @NotNull ItemTag<String> String(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getString(key),
                (nbtCompound, value) -> nbtCompound.setString(key, value));
    }

    public static @NotNull ItemTag<NBT> NBT(@NotNull String key) {
        return new ItemTag<>(key,
                nbt -> {
                    var currentNBT = nbt.get(key);

                    // Avoid a NPE when cloning a null variable.
                    if (currentNBT == null) {
                        return null;
                    }

                    return currentNBT.deepClone();
                },
                ((nbt, value) -> nbt.set(key, value.deepClone())));
    }

    public static @NotNull ItemTag<int[]> IntArray(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getIntArray(key),
                (nbtCompound, value) -> nbtCompound.setIntArray(key, value));
    }

    public static @NotNull ItemTag<long[]> LongArray(@NotNull String key) {
        return new ItemTag<>(key,
                nbtCompound -> nbtCompound.getLongArray(key),
                (nbtCompound, value) -> nbtCompound.setLongArray(key, value));
    }

}
