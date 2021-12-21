package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.SNBTParser;

import java.io.StringReader;
import java.util.Objects;
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

    /**
     * Handles the snbt of the tag holder.
     * <p>
     * Writing will override all tags. Proceed with caution.
     */
    @ApiStatus.Experimental
    public static final Tag<String> SNBT = new Tag<>(null, NBTCompound::toSNBT, (original, snbt) -> {
        try {
            final var updated = new SNBTParser(new StringReader(snbt)).parse();
            if (!(updated instanceof NBTCompound updatedCompound))
                throw new IllegalArgumentException("'" + snbt + "' is not a compound!");
            original.clear();
            updatedCompound.getKeys().forEach(s ->
                    original.set(s, Objects.requireNonNull(updatedCompound.get(s))));
        } catch (NBTException e) {
            e.printStackTrace();
        }
    }, null);

    /**
     * Handles the complete tag holder compound.
     * <p>
     * Writing will override all tags. Proceed with caution.
     */
    @ApiStatus.Experimental
    public static final Tag<NBTCompound> NBT = new Tag<>(null, NBTCompound::deepClone, (original, updated) -> {
        original.clear();
        updated.getKeys().forEach(s -> original.set(s, Objects.requireNonNull(updated.get(s))));
    }, null);

    private final String key;
    private final Function<NBTCompound, T> readFunction;
    private final BiConsumer<NBTCompound, T> writeConsumer;

    private final Supplier<T> defaultValue;

    protected Tag(@Nullable String key,
                  @NotNull Function<NBTCompound, T> readFunction,
                  @Nullable BiConsumer<NBTCompound, T> writeConsumer,
                  @Nullable Supplier<T> defaultValue) {
        this.key = key;
        this.readFunction = readFunction;
        this.writeConsumer = Objects.requireNonNullElse(writeConsumer, (compound, t) -> {
        });
        this.defaultValue = defaultValue;
    }

    protected Tag(@Nullable String key,
                  @NotNull Function<NBTCompound, T> readFunction,
                  @Nullable BiConsumer<NBTCompound, T> writeConsumer) {
        this(key, readFunction, writeConsumer, null);
    }

    /**
     * Returns the key used to navigate inside the holder nbt.
     * <p>
     * Can be null if unused (e.g. {@link #View(TagSerializer)}, {@link #SNBT} and {@link #NBT}).
     *
     * @return the tag key
     */
    public @Nullable String getKey() {
        return key;
    }

    @Contract(value = "_ -> new", pure = true)
    public Tag<T> defaultValue(@NotNull Supplier<T> defaultValue) {
        return new Tag<>(key, readFunction, writeConsumer, defaultValue);
    }

    @Contract(value = "_ -> new", pure = true)
    public Tag<T> defaultValue(@NotNull T defaultValue) {
        return defaultValue(() -> defaultValue);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public <R> Tag<R> map(@NotNull Function<T, R> readMap,
                          @NotNull Function<R, T> writeMap) {
        return new Tag<>(key,
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
        T result = readFunction.apply(nbtCompound);
        if (result == null) {
            final var supplier = defaultValue;
            result = supplier != null ? supplier.get() : null;
        }
        return result;
    }

    public void write(@NotNull NBTCompound nbtCompound, @Nullable T value) {
        if (key == null || value != null) {
            this.writeConsumer.accept(nbtCompound, value);
        } else {
            nbtCompound.removeTag(key);
        }
    }

    public void writeUnsafe(@NotNull NBTCompound nbtCompound, @Nullable Object value) {
        write(nbtCompound, (T) value);
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

    public static <T extends NBT> @NotNull Tag<T> NBT(@NotNull String key) {
        return new Tag<>(key,
                nbt -> {
                    final var currentNBT = nbt.get(key);
                    // Avoid a NPE when cloning a null variable.
                    if (currentNBT == null) {
                        return null;
                    }
                    return (T) currentNBT.deepClone();
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

    /**
     * Create a wrapper around a compound.
     *
     * @param key        the tag key
     * @param serializer the tag serializer
     * @param <T>        the tag type
     * @return the created tag
     */
    public static <T> @NotNull Tag<T> Structure(@NotNull String key, @NotNull TagSerializer<T> serializer) {
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

    public static <T> @NotNull Tag<T> View(@NotNull TagSerializer<T> serializer) {
        return new Tag<>(null,
                nbtCompound -> serializer.read(TagReadable.fromCompound(nbtCompound)),
                (nbtCompound, value) -> serializer.write(TagWritable.fromCompound(nbtCompound), value));
    }

    /**
     * @deprecated use {@link #Structure(String, TagSerializer)} instead
     */
    @Deprecated
    public static <T> @NotNull Tag<T> Custom(@NotNull String key, @NotNull TagSerializer<T> serializer) {
        return Structure(key, serializer);
    }
}
