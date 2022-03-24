package net.minestom.server.tag;

import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.collection.IndexMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.ArrayList;
import java.util.Arrays;
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

    final List<PathEntry> path;
    final UnaryOperator<T> copy;

    public Tag(int index, String key,
               Function<NBT, T> readFunction,
               Function<T, NBT> writeFunction,
               @Nullable Supplier<T> defaultValue, @Nullable List<PathEntry> path,
               @Nullable UnaryOperator<T> copy) {
        //noinspection AssertWithSideEffects
        assert index == INDEX_MAP.get(key);
        this.key = key;
        this.readFunction = readFunction;
        this.writeFunction = writeFunction;
        this.defaultValue = defaultValue;
        this.index = index;
        this.path = path;
        this.copy = copy;
    }

    protected Tag(String key,
                  Function<NBT, T> readFunction,
                  Function<T, NBT> writeFunction,
                  @Nullable Supplier<T> defaultValue,
                  @Nullable List<PathEntry> path) {
        this(INDEX_MAP.get(key), key, readFunction, writeFunction, defaultValue, path, null);
    }

    static <T, N extends NBT> Tag<T> tag(@NotNull String key,
                                         @NotNull Function<N, T> readFunction,
                                         @NotNull Function<T, N> writeFunction) {
        return new Tag<>(key, (Function<NBT, T>) readFunction, (Function<T, NBT>) writeFunction, null, null);
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
        return new Tag<>(index, key, readFunction, writeFunction, defaultValue, path, copy);
    }

    @Contract(value = "_ -> new", pure = true)
    public Tag<T> defaultValue(@NotNull T defaultValue) {
        return defaultValue(() -> defaultValue);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public <R> Tag<R> map(@NotNull Function<T, R> readMap,
                          @NotNull Function<R, T> writeMap) {
        return new Tag<>(index, key,
                // Read
                readFunction.andThen(t -> {
                    if (t == null) return null;
                    return readMap.apply(t);
                }),
                // Write
                writeMap.andThen(writeFunction),
                // Default value
                () -> readMap.apply(createDefault()),
                path, null);
    }

    @ApiStatus.Experimental
    @Contract(value = "-> new", pure = true)
    public Tag<List<T>> list() {
        return new Tag<>(index, key,
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
                    List<NBT> nbtList = new ArrayList<>(size);
                    for (T t : write) {
                        final NBT nbt = writeFunction.apply(t);
                        if (type == null) {
                            type = (NBTType<NBT>) nbt.getID();
                        } else if (type != nbt.getID()) {
                            throw new IllegalArgumentException("All elements of the list must have the same type");
                        }
                        nbtList.add(nbt);
                    }
                    return NBT.List(type, nbtList);
                }, null, path, List::copyOf);
    }

    @ApiStatus.Experimental
    @Contract(value = "_ -> new", pure = true)
    public Tag<T> path(@NotNull String @Nullable ... path) {
        final List<PathEntry> entries = path != null ? Arrays.stream(path).map(s -> new PathEntry(s, INDEX_MAP.get(s))).toList() : null;
        return new Tag<>(index, key, readFunction, writeFunction, defaultValue, entries, copy);
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
