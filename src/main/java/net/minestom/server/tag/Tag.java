package net.minestom.server.tag;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final Map<String, Integer> INDEX_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger INDEX = new AtomicInteger();

    private final String key;
    private final Function<NBTCompoundLike, T> readFunction;
    private final BiConsumer<MutableNBTCompound, T> writeConsumer;
    private final Supplier<T> defaultValue;

    final int index;

    protected Tag(@Nullable String key,
                  @NotNull Function<NBTCompoundLike, T> readFunction,
                  @NotNull BiConsumer<MutableNBTCompound, T> writeConsumer,
                  @Nullable Supplier<T> defaultValue) {
        this.key = key;
        this.readFunction = readFunction;
        this.writeConsumer = writeConsumer;
        this.defaultValue = defaultValue;

        this.index = INDEX_MAP.computeIfAbsent(key, k -> INDEX.getAndIncrement());
    }

    protected Tag(@Nullable String key,
                  @NotNull Function<NBTCompoundLike, T> readFunction,
                  @NotNull BiConsumer<MutableNBTCompound, T> writeConsumer) {
        this(key, readFunction, writeConsumer, null);
    }

    /**
     * Returns the key used to navigate inside the holder nbt.
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

    public @Nullable T read(@NotNull NBTCompoundLike nbtCompound) {
        T result = readFunction.apply(nbtCompound);
        if (result == null) result = createDefault();
        return result;
    }

    T createDefault() {
        final var supplier = defaultValue;
        return supplier != null ? supplier.get() : null;
    }

    public void write(@NotNull MutableNBTCompound nbtCompound, @Nullable T value) {
        if (key == null || value != null) {
            this.writeConsumer.accept(nbtCompound, value);
        } else {
            nbtCompound.remove(key);
        }
    }

    public void writeUnsafe(@NotNull MutableNBTCompound nbtCompound, @Nullable Object value) {
        //noinspection unchecked
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

    public static @NotNull Tag<String> String(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getString(key),
                (nbtCompound, value) -> nbtCompound.setString(key, value));
    }

    public static <T extends NBT> @NotNull Tag<T> NBT(@NotNull String key) {
        //noinspection unchecked
        return new Tag<>(key,
                nbt -> (T) nbt.get(key),
                ((nbt, value) -> nbt.set(key, value)));
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
                    final NBTCompound compound = nbtCompound.getCompound(key);
                    if (compound == null) return null;
                    return serializer.read(TagHandler.fromCompound(compound));
                },
                (nbtCompound, value) -> {
                    MutableNBTCompound mutableCopy = nbtCompound.get(key) instanceof NBTCompound c ?
                            c.toMutableCompound() : new MutableNBTCompound();
                    var handler = TagHandler.fromCompound(mutableCopy);
                    serializer.write(handler, value);
                    nbtCompound.set(key, handler.asCompound());
                });
    }

    public static <T> @NotNull Tag<T> View(@NotNull TagSerializer<T> serializer) {
        return new Tag<>(null,
                nbtCompound -> serializer.read(TagHandler.fromCompound(nbtCompound)),
                (nbtCompound, value) -> serializer.write(TagHandler.fromCompound(nbtCompound), value));
    }

    public static @NotNull Tag<ItemStack> ItemStack(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> ItemStack.fromItemNBT(nbtCompound.getCompound(key)),
                (nbtCompound, value) -> nbtCompound.set(key, value.toItemNBT()));
    }

    /**
     * @deprecated use {@link Tag#NBT(String)} with {@link NBT#ByteArray(byte...)}
     */
    @Deprecated
    public static @NotNull Tag<byte[]> ByteArray(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getByteArray(key).copyArray(),
                (nbtCompound, value) -> nbtCompound.setByteArray(key, value));
    }

    /**
     * @deprecated use {@link Tag#NBT(String)} with {@link NBT#IntArray(int...)}
     */
    @Deprecated
    public static @NotNull Tag<int[]> IntArray(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getIntArray(key).copyArray(),
                (nbtCompound, value) -> nbtCompound.setIntArray(key, value));
    }

    /**
     * @deprecated use {@link Tag#NBT(String)} with {@link NBT#LongArray(long...)}
     */
    @Deprecated
    public static @NotNull Tag<long[]> LongArray(@NotNull String key) {
        return new Tag<>(key,
                nbtCompound -> nbtCompound.getLongArray(key).copyArray(),
                (nbtCompound, value) -> nbtCompound.setLongArray(key, value));
    }

    /**
     * @deprecated use {@link #Structure(String, TagSerializer)} instead
     */
    @Deprecated
    public static <T> @NotNull Tag<T> Custom(@NotNull String key, @NotNull TagSerializer<T> serializer) {
        return Structure(key, serializer);
    }
}
