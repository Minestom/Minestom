package net.minestom.server.tag;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Tag<T extends @UnknownNullability Object> permits TagImpl {
    static Tag<Byte> Byte(String key) {
        return TagImpl.tag(key, Serializers.BYTE);
    }

    static Tag<Boolean> Boolean(String key) {
        return TagImpl.tag(key, Serializers.BOOLEAN);
    }

    static Tag<Short> Short(String key) {
        return TagImpl.tag(key, Serializers.SHORT);
    }

    static Tag<Integer> Integer(String key) {
        return TagImpl.tag(key, Serializers.INT);
    }

    static Tag<Long> Long(String key) {
        return TagImpl.tag(key, Serializers.LONG);
    }

    static Tag<Float> Float(String key) {
        return TagImpl.tag(key, Serializers.FLOAT);
    }

    static Tag<Double> Double(String key) {
        return TagImpl.tag(key, Serializers.DOUBLE);
    }

    static Tag<String> String(String key) {
        return TagImpl.tag(key, Serializers.STRING);
    }

    static Tag<UUID> UUID(String key) {
        return TagImpl.tag(key, Serializers.UUID);
    }

    static Tag<ItemStack> ItemStack(String key) {
        return TagImpl.tag(key, Serializers.ITEM);
    }

    static Tag<Component> Component(String key) {
        return TagImpl.tag(key, Serializers.COMPONENT);
    }

    /**
     * Creates a flexible tag able to read and write any {@link BinaryTag} objects.
     * <p>
     * Specialized tags are recommended if the type is known as conversion will be required both way (read and write).
     */
    static Tag<BinaryTag> NBT(String key) {
        return TagImpl.tag(key, Serializers.NBT_ENTRY);
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
    static <T> Tag<T> Structure(String key, TagSerializer<T> serializer) {
        return TagImpl.fromSerializer(key, serializer);
    }

    /**
     * Specialized Structure tag affecting the src of the handler (i.e. overwrite all its data).
     * <p>
     * Must be used with care.
     */
    static <T> Tag<T> View(TagSerializer<T> serializer) {
        return Structure("", serializer);
    }

    @ApiStatus.Experimental
    static <T extends Record> Tag<T> Structure(String key, Class<T> type) {
        return Structure(key, TagRecord.serializer(type));
    }

    @ApiStatus.Experimental
    static <T extends Record> Tag<T> View(Class<T> type) {
        return View(TagRecord.serializer(type));
    }

    /**
     * Creates a transient tag with the specified key. This tag does not get serialized
     * to NBT (Named Binary Tag) format and is not sent to the client. Unlike other tags,
     * which are serialized, transient tags are used for temporary data
     * that only needs to exist on the server side.
     *
     * @param <T> The type of the tag's value.
     * @param key The key.
     * @return A transient tag with the key.
     */
    static <T> Tag<T> Transient(String key) {
        //noinspection unchecked
        return (Tag<T>) TagImpl.tag(key, Serializers.EMPTY);
    }

    /**
     * Use {@link #key()} instead
     * @return the key
     * @deprecated misleading non-record component, use {@link #key()} instead.
     */
    @Deprecated
    String getKey();

    /**
     * Returns the key for the Tag
     * <br>
     * Same key specified during the creation.
     * @return the key to use
     */
    String key();

    @Contract(value = "_ -> new", pure = true)
    Tag<T> defaultValue(Supplier<T> defaultValue);

    @Contract(value = "_ -> new", pure = true)
    Tag<T> defaultValue(T defaultValue);

    @Contract(value = "_, _ -> new", pure = true)
    <R> Tag<R> map(Function<T, R> readMap,
                   Function<R, T> writeMap);

    @Contract(value = "-> new", pure = true)
    Tag<List<T>> list();

    @Contract(value = "_ -> new", pure = true)
    Tag<T> path(String @Nullable ... path);

    T read(CompoundBinaryTag nbt);

    void write(CompoundBinaryTag.Builder nbtCompound, T value);

    void writeUnsafe(CompoundBinaryTag.Builder nbtCompound, @Nullable Object value);

    boolean isView();

    boolean shareValue(Tag<?> other);

    T createDefault();

    T copyValue(T value);
}
