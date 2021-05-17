package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a key to retrieve or change a value.
 * <p>
 * All tags are serializable.
 *
 * @param <T> the tag type
 */
public final class Tag<T> {

    private final String key;
    private final Function<NBTCompound, T> readFunction;
    private final BiConsumer<NBTCompound, T> writeConsumer;
    private volatile Supplier<T> defaultValue;

    private Tag(@NotNull String key,
                @NotNull Function<NBTCompound, T> readFunction,
                @NotNull BiConsumer<NBTCompound, T> writeConsumer) {
        this.key = key;
        this.readFunction = readFunction;
        this.writeConsumer = writeConsumer;
    }

    public @NotNull String getKey() {
        return key;
    }

    public Tag<T> defaultValue(@NotNull Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Tag<T> defaultValue(@NotNull T defaultValue) {
        defaultValue(() -> defaultValue);
        return this;
    }

    public @Nullable T read(@NotNull NBTCompound nbtCompound) {
        if (nbtCompound.containsKey(key)) {
            return readFunction.apply(nbtCompound);
        } else {
            final var supplier = defaultValue;
            return supplier != null ? supplier.get() : null;
        }
    }

    public void write(@NotNull NBTCompound nbtCompound, @Nullable T value) {
        if (value != null) {
            this.writeConsumer.accept(nbtCompound, value);
        } else {
            nbtCompound.removeTag(key);
        }
    }

    public static @NotNull Tag<Byte> Byte(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getByte(key),
                (nbtCompound, value) -> nbtCompound.setByte(key, value));
    }

    public static @NotNull Tag<Short> Short(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getShort(key),
                (nbtCompound, value) -> nbtCompound.setShort(key, value));
    }

    public static @NotNull Tag<Integer> Integer(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getInt(key),
                (nbtCompound, integer) -> nbtCompound.setInt(key, integer));
    }

    public static @NotNull Tag<Long> Long(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getLong(key),
                (nbtCompound, value) -> nbtCompound.setLong(key, value));
    }

    public static @NotNull Tag<Float> Float(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getFloat(key),
                (nbtCompound, value) -> nbtCompound.setFloat(key, value));
    }

    public static @NotNull Tag<Double> Double(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getDouble(key),
                (nbtCompound, value) -> nbtCompound.setDouble(key, value));
    }

    public static @NotNull Tag<byte[]> ByteArray(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getByteArray(key),
                (nbtCompound, value) -> nbtCompound.setByteArray(key, value));
    }

    public static @NotNull Tag<String> String(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getString(key),
                (nbtCompound, value) -> nbtCompound.setString(key, value));
    }

    public static @NotNull Tag<NBT> NBT(@NotNull String key) {
        return new Tag<>(key,
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

    public static @NotNull Tag<int[]> IntArray(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getIntArray(key),
                (nbtCompound, value) -> nbtCompound.setIntArray(key, value));
    }

    public static @NotNull Tag<long[]> LongArray(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getLongArray(key),
                (nbtCompound, value) -> nbtCompound.setLongArray(key, value));
    }

    public static <T> @NotNull Tag<T> Custom(@NotNull String key, @NotNull TagSerializer<T> serializer) {
        return new Tag<>(key,
                nbtCompound -> serializer.read(TagReader.fromCompound(nbtCompound)),
                (nbtCompound, value) -> serializer.write(TagWriter.fromCompound(nbtCompound), value));
    }
}
