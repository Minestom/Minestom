package net.minestom.server.tag;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final Function<NBT, T> readFunction;
    private final Function<T, NBT> writeFunction;
    private final Supplier<T> defaultValue;

    final int index;

    protected Tag(@NotNull String key,
                  @NotNull Function<NBT, T> readFunction,
                  @NotNull Function<T, NBT> writeFunction,
                  @Nullable Supplier<T> defaultValue) {
        this.key = key;
        this.readFunction = readFunction;
        this.writeFunction = writeFunction;
        this.defaultValue = defaultValue;

        this.index = INDEX_MAP.computeIfAbsent(key, k -> INDEX.getAndIncrement());
    }

    protected Tag(@NotNull String key,
                  @NotNull Function<NBT, T> readFunction,
                  @NotNull Function<T, NBT> writeFunction) {
        this(key, readFunction, writeFunction, null);
    }

    /**
     * Returns the key used to navigate inside the holder nbt.
     *
     * @return the tag key
     */
    public @NotNull String getKey() {
        return key;
    }

    @Contract(value = "_ -> new", pure = true)
    public Tag<T> defaultValue(@NotNull Supplier<T> defaultValue) {
        return new Tag<>(key, readFunction, writeFunction, defaultValue);
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
                readFunction.andThen(t -> {
                    if (t == null) return null;
                    return readMap.apply(t);
                }),
                // Write
                writeMap.andThen(writeFunction),
                // Default value
                () -> readMap.apply(createDefault()));
    }

    public @Nullable T read(@NotNull NBTCompoundLike nbt) {
        final String key = this.key;
        if (key.isEmpty()) {
            // Special handling for view tag
            return convertToValue((NBT) nbt);
        }
        final NBT subTag = nbt.get(key);
        return convertToValue(subTag);
    }

    T createDefault() {
        final var supplier = defaultValue;
        return supplier != null ? supplier.get() : null;
    }

    public void write(@NotNull MutableNBTCompound nbtCompound, @Nullable T value) {
        final String key = this.key;
        if (value != null) {
            final NBT nbt = writeFunction.apply(value);
            if (key.isEmpty()) nbtCompound.copyFrom((NBTCompoundLike) nbt);
            else nbtCompound.set(key, nbt);
        } else {
            if (key.isEmpty()) nbtCompound.clear();
            else nbtCompound.remove(key);
        }
    }

    public void writeUnsafe(@NotNull MutableNBTCompound nbtCompound, @Nullable Object value) {
        //noinspection unchecked
        write(nbtCompound, (T) value);
    }

    T convertToValue(NBT nbt) {
        if (nbt == null) return createDefault();
        final T result = readFunction.apply(nbt);
        return result != null ? result : createDefault();
    }

    NBT convertToNbt(T value) {
        return writeFunction.apply(value);
    }

    public static @NotNull Tag<Byte> Byte(@NotNull String key) {
        return new Tag<>(key, nbt -> ((NBTByte) nbt).getValue(), NBT::Byte);
    }

    public static @NotNull Tag<Short> Short(@NotNull String key) {
        return new Tag<>(key, nbt -> ((NBTShort) nbt).getValue(), NBT::Short);
    }

    public static @NotNull Tag<Integer> Integer(@NotNull String key) {
        return new Tag<>(key, nbt -> ((NBTInt) nbt).getValue(), NBT::Int);
    }

    public static @NotNull Tag<Long> Long(@NotNull String key) {
        return new Tag<>(key, nbt -> ((NBTLong) nbt).getValue(), NBT::Long);
    }

    public static @NotNull Tag<Float> Float(@NotNull String key) {
        return new Tag<>(key, nbt -> ((NBTFloat) nbt).getValue(), NBT::Float);
    }

    public static @NotNull Tag<Double> Double(@NotNull String key) {
        return new Tag<>(key, nbt -> ((NBTDouble) nbt).getValue(), NBT::Double);
    }

    public static @NotNull Tag<String> String(@NotNull String key) {
        return new Tag<>(key, nbt -> ((NBTString) nbt).getValue(), NBT::String);
    }

    public static <T extends NBT> @NotNull Tag<T> NBT(@NotNull String key) {
        //noinspection unchecked
        return new Tag<>(key, nbt -> (T) nbt, t -> t);
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
                nbt -> serializer.read(TagHandler.fromCompound((NBTCompoundLike) nbt)),
                (value) -> {
                    TagHandler handler = TagHandler.newHandler();
                    serializer.write(handler, value);
                    return handler.asCompound();
                });
    }

    public static <T> @NotNull Tag<T> View(@NotNull TagSerializer<T> serializer) {
        // TODO special id
        return new Tag<>("",
                nbt -> serializer.read(TagHandler.fromCompound((NBTCompoundLike) nbt)),
                (value) -> {
                    TagHandler handler = TagHandler.newHandler();
                    serializer.write(handler, value);
                    return handler.asCompound();
                });
    }

    public static @NotNull Tag<ItemStack> ItemStack(@NotNull String key) {
        return new Tag<>(key, nbt -> ItemStack.fromItemNBT((NBTCompound) nbt), ItemStack::toItemNBT);
    }

    /**
     * @deprecated use {@link Tag#NBT(String)} with {@link NBT#ByteArray(byte...)}
     */
    @Deprecated
    public static @NotNull Tag<byte[]> ByteArray(@NotNull String key) {
        return new Tag<>(key, nbt -> ((NBTByteArray) nbt).getValue().copyArray(), NBT::ByteArray);
    }

    /**
     * @deprecated use {@link Tag#NBT(String)} with {@link NBT#IntArray(int...)}
     */
    @Deprecated
    public static @NotNull Tag<int[]> IntArray(@NotNull String key) {
        return new Tag<>(key, nbt -> ((NBTIntArray) nbt).getValue().copyArray(), NBT::IntArray);
    }

    /**
     * @deprecated use {@link Tag#NBT(String)} with {@link NBT#LongArray(long...)}
     */
    @Deprecated
    public static @NotNull Tag<long[]> LongArray(@NotNull String key) {
        return new Tag<>(key, nbt -> ((NBTLongArray) nbt).getValue().copyArray(), NBT::LongArray);
    }

    /**
     * @deprecated use {@link #Structure(String, TagSerializer)} instead
     */
    @Deprecated
    public static <T> @NotNull Tag<T> Custom(@NotNull String key, @NotNull TagSerializer<T> serializer) {
        return Structure(key, serializer);
    }
}
