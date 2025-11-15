package net.minestom.server.tag;

import net.kyori.adventure.nbt.*;
import net.minestom.server.utils.collection.AutoIncrementMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public record TagImpl<T>(int index, String key,
                         Function<?, ?> readComparator,
                         Serializers.Entry<T, BinaryTag> entry,
                         // Optional properties
                         @Nullable Supplier<@Nullable T> defaultValue,
                         PathEntry @Nullable [] path,
                         @Nullable UnaryOperator<T> copy, int listScope) implements Tag<T> {
    private static final AutoIncrementMap<String> INDEX_MAP = new AutoIncrementMap<>();

    public TagImpl {
        assert index == INDEX_MAP.get(key);
    }

    @SuppressWarnings("unchecked")
    static <T, N extends BinaryTag> TagImpl<T> tag(String key, Serializers.Entry<T, N> entry) {
        return new TagImpl<>(INDEX_MAP.get(key), key, entry.reader(), (Serializers.Entry<T, BinaryTag>) entry,
                null, null, null, 0);
    }

    static <T> TagImpl<T> fromSerializer(String key, TagSerializer<T> serializer) {
        if (serializer instanceof TagRecord.Serializer<?> recordSerializer) {
            // Allow fast retrieval
            //noinspection unchecked
            return (TagImpl<T>) tag(key, recordSerializer.serializerEntry);
        }
        return tag(key, Serializers.fromTagSerializer(serializer));
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String key() {
        return key;
    }

    @Contract(value = "_ -> new", pure = true)
    @Override
    public Tag<T> defaultValue(Supplier<T> defaultValue) {
        return new TagImpl<>(index, key, readComparator, entry, defaultValue, path, copy, listScope);
    }

    @Contract(value = "_ -> new", pure = true)
    @Override
    public Tag<T> defaultValue(T defaultValue) {
        return defaultValue(() -> defaultValue);
    }

    @Contract(value = "_, _ -> new", pure = true)
    @Override
    public <R extends @UnknownNullability Object> Tag<R> map(Function<T, R> readMap,
                          Function<R, T> writeMap) {
        var entry = this.entry;
        final Function<BinaryTag, R> readFunction = entry.reader().andThen(t -> {
            if (t == null) return null;
            return readMap.apply(t);
        });
        final Function<R, BinaryTag> writeFunction = writeMap.andThen(entry.writer());
        return new TagImpl<>(index, key, readMap,
                new Serializers.Entry<>(entry.nbtType(), readFunction, writeFunction),
                // Default value
                () -> {
                    T defaultValue = createDefault();
                    if (defaultValue == null) return null;
                    return readMap.apply(defaultValue);
                },
                path, null, listScope);
    }

    @Contract(value = "-> new", pure = true)
    @Override
    public Tag<List<T>> list() {
        var entry = this.entry;
        var readFunction = entry.reader();
        var writeFunction = entry.writer();
        var listEntry = new Serializers.Entry<List<T>, ListBinaryTag>(
                BinaryTagTypes.LIST,
                read -> {
                    if (read.isEmpty()) return List.of();
                    return read.stream().map(readFunction).toList();
                },
                write -> {
                    if (write.isEmpty())
                        return ListBinaryTag.empty();
                    final List<BinaryTag> list = write.stream().map(writeFunction).toList();
                    final BinaryTagType<?> type = list.getFirst().type();
                    return ListBinaryTag.listBinaryTag(type, list);
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
        return new TagImpl<>(index, key, readComparator, (Serializers.Entry) listEntry,
                null, path, co, listScope + 1);
    }

    @Contract(value = "_ -> new", pure = true)
    @Override
    public Tag<T> path(String @Nullable ... path) {
        if (path == null || path.length == 0) {
            return new TagImpl<>(index, key, readComparator, entry, defaultValue, null, copy, listScope);
        }
        PathEntry[] pathEntries = new PathEntry[path.length];
        for (int i = 0; i < path.length; i++) {
            final String name = path[i];
            if (name == null || name.isEmpty()) throw new IllegalArgumentException("Path must not be empty: " + Arrays.toString(path));
            pathEntries[i] = new PathEntry(name, INDEX_MAP.get(name));
        }
        return new TagImpl<>(index, key, readComparator, entry, defaultValue, pathEntries, copy, listScope);
    }

    @Override
    public @Nullable T read(CompoundBinaryTag nbt) {
        final BinaryTag readable = isView() ? nbt : nbt.get(key);
        final T result;
        try {
            if (readable == null || (result = entry.read(readable)) == null)
                return createDefault();
            return result;
        } catch (ClassCastException e) {
            return createDefault();
        }
    }

    @Override
    public void write(CompoundBinaryTag.Builder nbtCompound, @Nullable T value) {
        if (value != null) {
            final BinaryTag nbt = entry.write(value);
            if (isView()) nbtCompound.put((CompoundBinaryTag) nbt);
            else nbtCompound.put(key, nbt);
        } else {
            if (isView()) {
                // Adventure compound builder doesn't currently have a clear method.
                nbtCompound.build().keySet().forEach(nbtCompound::remove);
            } else nbtCompound.remove(key);
        }
    }

    @Override
    public void writeUnsafe(CompoundBinaryTag.Builder nbtCompound, @Nullable Object value) {
        //noinspection unchecked
        write(nbtCompound, (T) value);
    }

    @Override
    public boolean isView() {
        return key.isEmpty();
    }

    @Override
    public boolean shareValue(Tag<?> other) {
        if (this == other) return true;
        if (!(other instanceof TagImpl<?> otherImpl)) return false;
        // Tags are not strictly the same, compare readers
        if (this.listScope != otherImpl.listScope) return false;
        return this.readComparator == otherImpl.readComparator;
    }

    @Override
    public @Nullable T createDefault() {
        final Supplier<T> supplier = defaultValue;
        return supplier != null ? supplier.get() : null;
    }

    @Override
    public T copyValue(T value) {
        final UnaryOperator<T> copier = copy;
        return copier != null ? copier.apply(value) : value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagImpl<?> tag)) return false;
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

    public record PathEntry(String name, int index) {
    }
}
