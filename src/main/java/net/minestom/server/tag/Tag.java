package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
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
@ApiStatus.NonExtendable
public class Tag<T> {

    private final String key;
    private final Function<NBTCompound, T> readFunction;
    private final BiConsumer<NBTCompound, T> writeConsumer;

    private final Supplier<T> defaultValue;

    protected Tag(@NotNull String key,
                  @NotNull Function<NBTCompound, T> readFunction,
                  @NotNull BiConsumer<NBTCompound, T> writeConsumer,
                  @Nullable Supplier<T> defaultValue) {
        this.key = key;
        this.readFunction = readFunction;
        this.writeConsumer = writeConsumer;
        this.defaultValue = defaultValue;
    }

    protected Tag(@NotNull String key,
                  @NotNull Function<NBTCompound, T> readFunction,
                  @NotNull BiConsumer<NBTCompound, T> writeConsumer) {
        this(key, readFunction, writeConsumer, null);
    }

    public @NotNull String getKey() {
        return key;
    }

    @Contract(value = "_ -> new", pure = true)
    public Tag<T> defaultValue(@NotNull Supplier<T> defaultValue) {
        return new Tag<>(key, readFunction, writeConsumer, defaultValue);
    }

    @Contract(value = "_ -> new", pure = true)
    public Tag<T> defaultValue(@NotNull T defaultValue) {
        return new Tag<>(key, readFunction, writeConsumer, () -> defaultValue);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public <R> Tag<R> map(@NotNull Function<T, R> readMap,
                          @NotNull Function<R, T> writeMap) {
        return new Tag<R>(key,
                // Read
                nbtCompound -> {
                    final var old = readFunction.apply(nbtCompound);
                    if (old == null) {
                        return null;
                    }
                    return readMap.apply(old);
                },
                // Write
                (nbtCompound, r) -> {
                    var n = writeMap.apply(r);
                    writeConsumer.accept(nbtCompound, n);
                },
                // Default value
                () -> {
                    if (defaultValue == null) {
                        return null;
                    }
                    var old = defaultValue.get();
                    return readMap.apply(old);
                });
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
                nbtCompound -> {
                    final var compound = nbtCompound.getCompound(key);
                    if (compound == null) {
                        return null;
                    }
                    return serializer.read(TagReadable.fromCompound(compound));
                },
                (nbtCompound, value) -> {
                    var compound = nbtCompound.getCompound(key);
                    if (compound == null) {
                        compound = new NBTCompound();
                        nbtCompound.set(key, compound);
                    }
                    serializer.write(TagWritable.fromCompound(compound), value);
                });
    }
}
