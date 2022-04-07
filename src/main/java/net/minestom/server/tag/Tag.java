package net.minestom.server.tag;

import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.collection.IndexMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;
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
    final Serializers.Entry<T, NBT> entry;
    private final Supplier<T> defaultValue;

    final Function<?, ?> readComparator;
    // Optional properties
    final PathEntry[] path;
    final UnaryOperator<T> copy;
    final int listScope;

    Tag(int index, String key,
        Function<?, ?> readComparator,
        Serializers.Entry<T, NBT> entry,
        Supplier<T> defaultValue, PathEntry[] path, UnaryOperator<T> copy, int listScope) {
        assert index == INDEX_MAP.get(key);
        this.index = index;
        this.key = key;
        this.readComparator = readComparator;
        this.entry = entry;
        this.defaultValue = defaultValue;
        this.path = path;
        this.copy = copy;
        this.listScope = listScope;
    }

    static <T, N extends NBT> Tag<T> tag(@NotNull String key, @NotNull Serializers.Entry<T, N> entry) {
        return new Tag<>(INDEX_MAP.get(key), key, entry.read(), (Serializers.Entry<T, NBT>) entry,
                null, null, null, 0);
    }

    static <T> Tag<T> fromSerializer(@NotNull String key, @NotNull TagSerializer<T> serializer) {
        if (serializer instanceof TagRecord.Serializer recordSerializer) {
            // Allow fast retrieval
            return tag(key, recordSerializer.serializerEntry);
        }
        return tag(key, Serializers.fromTagSerializer(serializer));
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
        return new Tag<>(index, key, readComparator, entry, defaultValue, path, copy, listScope);
    }

    @Contract(value = "_ -> new", pure = true)
    public Tag<T> defaultValue(@NotNull T defaultValue) {
        return defaultValue(() -> defaultValue);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public <R> Tag<R> map(@NotNull Function<T, R> readMap,
                          @NotNull Function<R, T> writeMap) {
        var entry = this.entry;
        final Function<NBT, R> readFunction = entry.read().andThen(t -> {
            if (t == null) return null;
            return readMap.apply(t);
        });
        final Function<R, NBT> writeFunction = writeMap.andThen(entry.write());
        return new Tag<>(index, key, readMap,
                new Serializers.Entry<>(readFunction, writeFunction),
                // Default value
                () -> readMap.apply(createDefault()),
                path, null, listScope);
    }

    @ApiStatus.Experimental
    @Contract(value = "-> new", pure = true)
    public Tag<List<T>> list() {
        var entry = this.entry;
        var readFunction = entry.read();
        var writeFunction = entry.write();
        var listEntry = new Serializers.Entry<List<T>, NBT>(
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
                });
        UnaryOperator<List<T>> co = this.copy != null ? ts -> {
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
        } : List::copyOf;
        return new Tag<>(index, key, readComparator, listEntry, null, path, co, listScope + 1);
    }

    @ApiStatus.Experimental
    @Contract(value = "_ -> new", pure = true)
    public Tag<T> path(@NotNull String @Nullable ... path) {
        if (path == null || path.length == 0) {
            return new Tag<>(index, key, readComparator, entry, defaultValue, null, copy, listScope);
        }
        PathEntry[] pathEntries = new PathEntry[path.length];
        for (int i = 0; i < path.length; i++) {
            var name = path[i];
            pathEntries[i] = new PathEntry(name, INDEX_MAP.get(name));
        }
        return new Tag<>(index, key, readComparator, entry, defaultValue, pathEntries, copy, listScope);
    }

    public @Nullable T read(@NotNull NBTCompoundLike nbt) {
        final String key = this.key;
        final NBT readable = key.isEmpty() ? nbt.toCompound() : nbt.get(key);
        final T result;
        try {
            if (readable == null || (result = entry.read().apply(readable)) == null)
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
            final NBT nbt = entry.write().apply(value);
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
        if (this == other) return true;
        // Tags are not strictly the same, compare readers
        if (this.listScope != other.listScope)
            return false;
        return this.readComparator == other.readComparator;
    }

    public static @NotNull Tag<Byte> Byte(@NotNull String key) {
        return tag(key, Serializers.BYTE);
    }

    public static @NotNull Tag<Short> Short(@NotNull String key) {
        return tag(key, Serializers.SHORT);
    }

    public static @NotNull Tag<Integer> Integer(@NotNull String key) {
        return tag(key, Serializers.INT);
    }

    public static @NotNull Tag<Long> Long(@NotNull String key) {
        return tag(key, Serializers.LONG);
    }

    public static @NotNull Tag<Float> Float(@NotNull String key) {
        return tag(key, Serializers.FLOAT);
    }

    public static @NotNull Tag<Double> Double(@NotNull String key) {
        return tag(key, Serializers.DOUBLE);
    }

    public static @NotNull Tag<String> String(@NotNull String key) {
        return tag(key, Serializers.STRING);
    }

    public static <T extends NBT> @NotNull Tag<T> NBT(@NotNull String key) {
        return tag(key, (Serializers.Entry<T, ? extends NBT>) Serializers.NBT_ENTRY);
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
        return fromSerializer(key, serializer);
    }

    @ApiStatus.Experimental
    public static <T extends Record> @NotNull Tag<T> Structure(@NotNull String key, @NotNull Class<T> type) {
        assert type.isRecord();
        return fromSerializer(key, TagRecord.serializer(type));
    }

    public static <T> @NotNull Tag<T> View(@NotNull TagSerializer<T> serializer) {
        return Structure("", serializer);
    }

    public static @NotNull Tag<ItemStack> ItemStack(@NotNull String key) {
        return tag(key, Serializers.ITEM);
    }
}
