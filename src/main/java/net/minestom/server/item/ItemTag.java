package net.minestom.server.item;

import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @deprecated use {@link Tag}.
 */
@Deprecated
public class ItemTag<T> extends Tag<T> {

    protected ItemTag(@NotNull String key, @NotNull Function<NBTCompound, T> readFunction, @NotNull BiConsumer<NBTCompound, T> writeConsumer) {
        super(key, readFunction, writeConsumer);
    }

    public static @NotNull Tag<Byte> Byte(@NotNull String key) {
        return Tag.Byte(key);
    }

    public static @NotNull Tag<Short> Short(@NotNull String key) {
        return Tag.Short(key);
    }

    public static @NotNull Tag<Integer> Integer(@NotNull String key) {
        return Tag.Integer(key);
    }

    public static @NotNull Tag<Long> Long(@NotNull String key) {
        return Tag.Long(key);
    }

    public static @NotNull Tag<Float> Float(@NotNull String key) {
        return Tag.Float(key);
    }

    public static @NotNull Tag<Double> Double(@NotNull String key) {
        return Tag.Double(key);
    }

    public static @NotNull Tag<byte[]> ByteArray(@NotNull String key) {
        return Tag.ByteArray(key);
    }

    public static @NotNull Tag<String> String(@NotNull String key) {
        return Tag.String(key);
    }

    public static @NotNull Tag<NBT> NBT(@NotNull String key) {
        return Tag.NBT(key);
    }

    public static @NotNull Tag<int[]> IntArray(@NotNull String key) {
        return Tag.IntArray(key);
    }

    public static @NotNull Tag<long[]> LongArray(@NotNull String key) {
        return Tag.LongArray(key);
    }

}
