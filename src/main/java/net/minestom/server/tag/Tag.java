package net.minestom.server.tag;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.collection.AutoIncrementMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
    private static final AutoIncrementMap<String> INDEX_MAP = new AutoIncrementMap<>();

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
        return new Tag<>(INDEX_MAP.get(key), key, entry.reader(), (Serializers.Entry<T, NBT>) entry,
                null, null, null, 0);
    }

    static <T> Tag<T> fromSerializer(@NotNull String key, @NotNull TagSerializer<T> serializer) {
        if (serializer instanceof TagRecord.Serializer recordSerializer) {
            // Allow fast retrieval
            //noinspection unchecked
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
        final Function<NBT, R> readFunction = entry.reader().andThen(t -> {
            if (t == null) return null;
            return readMap.apply(t);
        });
        final Function<R, NBT> writeFunction = writeMap.andThen(entry.writer());
        return new Tag<>(index, key, readMap,
                new Serializers.Entry<>(entry.nbtType(), readFunction, writeFunction),
                // Default value
                () -> {
                    T defaultValue = createDefault();
                    if (defaultValue == null) return null;
                    return readMap.apply(defaultValue);
                },
                path, null, listScope);
    }

    @ApiStatus.Experimental
    @Contract(value = "-> new", pure = true)
    public Tag<List<T>> list() {
        var entry = this.entry;
        var readFunction = entry.reader();
        var writeFunction = entry.writer();
        var listEntry = new Serializers.Entry<List<T>, NBTList<?>>(
                NBTType.TAG_List,
                read -> {
                    if (read.isEmpty()) return List.of();
                    return read.asListView().stream().map(readFunction).toList();
                },
                write -> {
                    if (write.isEmpty())
                        return NBT.List(NBTType.TAG_String); // String is the default type for lists
                    final List<NBT> list = write.stream().map(writeFunction).toList();
                    final NBTType<?> type = list.get(0).getID();
                    return NBT.List(type, list);
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
        return new Tag<>(index, key, readComparator, Serializers.Entry.class.cast(listEntry),
                null, path, co, listScope + 1);
    }

    @ApiStatus.Experimental
    @Contract(value = "_ -> new", pure = true)
    public Tag<T> path(@NotNull String @Nullable ... path) {
        if (path == null || path.length == 0) {
            return new Tag<>(index, key, readComparator, entry, defaultValue, null, copy, listScope);
        }
        PathEntry[] pathEntries = new PathEntry[path.length];
        for (int i = 0; i < path.length; i++) {
            final String name = path[i];
            if (name == null || name.isEmpty())
                throw new IllegalArgumentException("Path must not be empty: " + Arrays.toString(path));
            pathEntries[i] = new PathEntry(name, INDEX_MAP.get(name));
        }
        return new Tag<>(index, key, readComparator, entry, defaultValue, pathEntries, copy, listScope);
    }

    public @Nullable T read(@NotNull NBTCompoundLike nbt) {
        final NBT readable = isView() ? nbt.toCompound() : nbt.get(key);
        final T result;
        try {
            if (readable == null || (result = entry.read(readable)) == null)
                return createDefault();
            return result;
        } catch (ClassCastException e) {
            return createDefault();
        }
    }

    public void write(@NotNull MutableNBTCompound nbtCompound, @Nullable T value) {
        if (value != null) {
            final NBT nbt = entry.write(value);
            if (isView()) nbtCompound.copyFrom((NBTCompoundLike) nbt);
            else nbtCompound.set(key, nbt);
        } else {
            if (isView()) nbtCompound.clear();
            else nbtCompound.remove(key);
        }
    }

    public void writeUnsafe(@NotNull MutableNBTCompound nbtCompound, @Nullable Object value) {
        //noinspection unchecked
        write(nbtCompound, (T) value);
    }

    final boolean isView() {
        return key.isEmpty();
    }

    final boolean shareValue(@NotNull Tag<?> other) {
        if (this == other) return true;
        // Tags are not strictly the same, compare readers
        if (this.listScope != other.listScope)
            return false;
        return this.readComparator == other.readComparator;
    }

    final T createDefault() {
        final Supplier<T> supplier = defaultValue;
        return supplier != null ? supplier.get() : null;
    }

    final T copyValue(@NotNull T value) {
        final UnaryOperator<T> copier = copy;
        return copier != null ? copier.apply(value) : value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag<?> tag)) return false;
        return index == tag.index &&
                listScope == tag.listScope &&
                readComparator.equals(tag.readComparator) &&
                Objects.equals(defaultValue, tag.defaultValue) &&
                Arrays.equals(path, tag.path) && Objects.equals(copy, tag.copy);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(index, readComparator, defaultValue, copy, listScope);
        result = 31 * result + Arrays.hashCode(path);
        return result;
    }

    public static @NotNull Tag<Byte> Byte(@NotNull String key) {
        return tag(key, Serializers.BYTE);
    }

    public static @NotNull Tag<Boolean> Boolean(@NotNull String key) {
        return tag(key, Serializers.BOOLEAN);
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

    @ApiStatus.Experimental
    public static @NotNull Tag<UUID> UUID(@NotNull String key) {
        return tag(key, Serializers.UUID);
    }

    public static @NotNull Tag<ItemStack> ItemStack(@NotNull String key) {
        return tag(key, Serializers.ITEM);
    }

    public static @NotNull Tag<Component> Component(@NotNull String key) {
        return tag(key, Serializers.COMPONENT);
    }

    /**
     * Creates a flexible tag able to read and write any {@link NBT} objects.
     * <p>
     * Specialized tags are recommended if the type is known as conversion will be required both way (read and write).
     */
    public static @NotNull Tag<NBT> NBT(@NotNull String key) {
        return tag(key, Serializers.NBT_ENTRY);
    }

    /**
     * Creates a tag containing multiple fields.
     * <p>
     * Those fields cannot be modified from an outside tag. (This is to prevent the backed object from becoming out of sync)
     *
     * @param key        the tag key
     * @param serializer the tag serializer
     * @param <T>        the tag type
     * @return the created tag
     */
    public static <T> @NotNull Tag<T> Structure(@NotNull String key, @NotNull TagSerializer<T> serializer) {
        return fromSerializer(key, serializer);
    }

    /**
     * Specialized Structure tag affecting the src of the handler (i.e. overwrite all its data).
     * <p>
     * Must be used with care.
     */
    public static <T> @NotNull Tag<T> View(@NotNull TagSerializer<T> serializer) {
        return Structure("", serializer);
    }

    @ApiStatus.Experimental
    public static <T extends Record> @NotNull Tag<T> Structure(@NotNull String key, @NotNull Class<T> type) {
        return Structure(key, TagRecord.serializer(type));
    }

    @ApiStatus.Experimental
    public static <T extends Record> @NotNull Tag<T> View(@NotNull Class<T> type) {
        return View(TagRecord.serializer(type));
    }
}
