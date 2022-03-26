package net.minestom.server.tag;

import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.collection.IndexMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Represents a key to retrieve or change a value.
 * <p>
 * All tags are serializable.
 *
 * @param <T> the tag type
 */
@ApiStatus.NonExtendable
public class Tag<T> {
    private static final IndexMap<String> INDEX_MAP = new IndexMap<>();

    record PathEntry(String name, int index) {
    }

    final int index;
    private final String key;
    final Function<NBT, T> readFunction;
    final Function<T, NBT> writeFunction;
    private final Supplier<T> defaultValue;

    final Function<?, ?> originalRead;
    // Optional properties
    final PathEntry[] path;
    final UnaryOperator<T> copy;
    final int listScope;

    Tag(int index, String key,
        Function<?, ?> originalRead,
        Function<NBT, T> readFunction, Function<T, NBT> writeFunction,
        Supplier<T> defaultValue, PathEntry[] path, UnaryOperator<T> copy, int listScope) {
        assert index == INDEX_MAP.get(key);
        this.index = index;
        this.key = key;
        this.originalRead = originalRead;
        this.readFunction = readFunction;
        this.writeFunction = writeFunction;
        this.defaultValue = defaultValue;
        this.path = path;
        this.copy = copy;
        this.listScope = listScope;
    }

    static <T, N extends NBT> Tag<T> tag(@NotNull String key,
                                         @NotNull Function<N, T> readFunction,
                                         @NotNull Function<T, N> writeFunction) {
        return new Tag<>(INDEX_MAP.get(key), key, readFunction,
                (Function<NBT, T>) readFunction, (Function<T, NBT>) writeFunction,
                null, null, null, 0);
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
        return new Tag<>(index, key, originalRead, readFunction, writeFunction, defaultValue, path, copy, listScope);
    }

    @Contract(value = "_ -> new", pure = true)
    public Tag<T> defaultValue(@NotNull T defaultValue) {
        return defaultValue(() -> defaultValue);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public <R> Tag<R> map(@NotNull Function<T, R> readMap,
                          @NotNull Function<R, T> writeMap) {
        return new Tag<>(index, key,
                readMap,
                // Read
                readFunction.andThen(t -> {
                    if (t == null) return null;
                    return readMap.apply(t);
                }),
                // Write
                writeMap.andThen(writeFunction),
                // Default value
                () -> readMap.apply(createDefault()),
                path, null, listScope);
    }

    @ApiStatus.Experimental
    @Contract(value = "-> new", pure = true)
    public Tag<List<T>> list() {
        return new Tag<>(index, key, originalRead,
                read -> {
                    var list = (NBTList<?>) read;
                    final int size = list.getSize();
                    if (size == 0)
                        return List.of();
                    T[] array = (T[]) new Object[size];
                    for (int i = 0; i < size; i++) {
                        array[i] = readFunction.apply(list.get(i));
                    }
                    return List.of(array);
                },
                write -> {
                    final int size = write.size();
                    if (size == 0)
                        return new NBTList<>(NBTType.TAG_String); // String is the default type for lists
                    NBTType<NBT> type = null;
                    NBT[] array = new NBT[size];
                    for (int i = 0; i < size; i++) {
                        final NBT nbt = writeFunction.apply(write.get(i));
                        if (type == null) {
                            type = (NBTType<NBT>) nbt.getID();
                        } else if (type != nbt.getID()) {
                            throw new IllegalArgumentException("All elements of the list must have the same type");
                        }
                        array[i] = nbt;
                    }
                    return NBT.List(type, List.of(array));
                }, null, path,
                copy != null ? ts -> {
                    final int size = ts.size();
                    T[] array = (T[]) new Object[size];
                    boolean shallowCopy = true;
                    for (int i = 0; i < size; i++) {
                        final T t = ts.get(i);
                        final T copy = this.copy.apply(t);
                        if (shallowCopy && copy != t) shallowCopy = false;
                        array[i] = copy;
                    }
                    return shallowCopy ? List.copyOf(ts) : List.of(array);
                } : List::copyOf, listScope + 1);
    }

    @ApiStatus.Experimental
    @Contract(value = "_ -> new", pure = true)
    public Tag<T> path(@NotNull String @Nullable ... path) {
        if (path == null || path.length == 0) {
            return new Tag<>(index, key, originalRead,
                    readFunction, writeFunction, defaultValue, null, copy, listScope);
        }
        PathEntry[] entries = new PathEntry[path.length];
        for (int i = 0; i < path.length; i++) {
            var name = path[i];
            entries[i] = new PathEntry(name, INDEX_MAP.get(name));
        }
        return new Tag<>(index, key, originalRead,
                readFunction, writeFunction, defaultValue, entries, copy, listScope);
    }

    public @Nullable T read(@NotNull NBTCompoundLike nbt) {
        final String key = this.key;
        final NBT readable = key.isEmpty() ? nbt.toCompound() : nbt.get(key);
        final T result;
        try {
            if (readable == null || (result = readFunction.apply(readable)) == null)
                return createDefault();
            return result;
        } catch (ClassCastException e) {
            return createDefault();
        }
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

    final boolean shareValue(@NotNull Tag<?> other) {
        // Verify if these 2 tags can share the same cached value
        // Key/Default value/Path are ignored
        return this == other || (this.originalRead == other.originalRead && this.listScope == other.listScope);
    }

    public static @NotNull Tag<Byte> Byte(@NotNull String key) {
        return tag(key, NBTByte::getValue, NBT::Byte);
    }

    public static @NotNull Tag<Short> Short(@NotNull String key) {
        return tag(key, NBTShort::getValue, NBT::Short);
    }

    public static @NotNull Tag<Integer> Integer(@NotNull String key) {
        return tag(key, NBTInt::getValue, NBT::Int);
    }

    public static @NotNull Tag<Long> Long(@NotNull String key) {
        return tag(key, NBTLong::getValue, NBT::Long);
    }

    public static @NotNull Tag<Float> Float(@NotNull String key) {
        return tag(key, NBTFloat::getValue, NBT::Float);
    }

    public static @NotNull Tag<Double> Double(@NotNull String key) {
        return tag(key, NBTDouble::getValue, NBT::Double);
    }

    public static @NotNull Tag<String> String(@NotNull String key) {
        return tag(key, NBTString::getValue, NBT::String);
    }

    public static <T extends NBT> @NotNull Tag<T> NBT(@NotNull String key) {
        return Tag.<T, T>tag(key, nbt -> nbt, t -> t);
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
        return tag(key,
                (NBTCompound compound) -> serializer.read(TagHandler.fromCompound(compound)),
                (value) -> {
                    TagHandler handler = TagHandler.newHandler();
                    serializer.write(handler, value);
                    return handler.asCompound();
                });
    }

    public static <T> @NotNull Tag<T> View(@NotNull TagSerializer<T> serializer) {
        return tag("",
                (NBTCompound compound) -> serializer.read(TagHandler.fromCompound(compound)),
                (value) -> {
                    TagHandler handler = TagHandler.newHandler();
                    serializer.write(handler, value);
                    return handler.asCompound();
                });
    }

    public static @NotNull Tag<ItemStack> ItemStack(@NotNull String key) {
        return tag(key, ItemStack::fromItemNBT, ItemStack::toItemNBT);
    }
}
