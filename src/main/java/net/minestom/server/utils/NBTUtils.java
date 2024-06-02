package net.minestom.server.utils;

import net.kyori.adventure.nbt.*;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class NBTUtils {
    private static final BinaryTagType<?>[] TYPES = new BinaryTagType[]{
            BinaryTagTypes.END,
            BinaryTagTypes.BYTE,
            BinaryTagTypes.SHORT,
            BinaryTagTypes.INT,
            BinaryTagTypes.LONG,
            BinaryTagTypes.FLOAT,
            BinaryTagTypes.DOUBLE,
            BinaryTagTypes.BYTE_ARRAY,
            BinaryTagTypes.STRING,
            BinaryTagTypes.LIST,
            BinaryTagTypes.COMPOUND,
            BinaryTagTypes.INT_ARRAY,
            BinaryTagTypes.LONG_ARRAY,
    };

    public static @NotNull BinaryTagType<?> nbtTypeFromId(byte id) {
        Check.argCondition(id < 0 || id >= TYPES.length, "Invalid NBT type id: " + id);
        return TYPES[id];
    }

    public static @NotNull Object nbtValueFromTag(@NotNull BinaryTag tag) {
        if (tag instanceof ByteBinaryTag byteTag) {
            return byteTag.value();
        } else if (tag instanceof ShortBinaryTag shortTag) {
            return shortTag.value();
        } else if (tag instanceof IntBinaryTag intTag) {
            return intTag.value();
        } else if (tag instanceof LongBinaryTag longTag) {
            return longTag.value();
        } else if (tag instanceof FloatBinaryTag floatTag) {
            return floatTag.value();
        } else if (tag instanceof DoubleBinaryTag doubleTag) {
            return doubleTag.value();
        } else if (tag instanceof ByteArrayBinaryTag byteArrayTag) {
            return byteArrayTag.value();
        } else if (tag instanceof StringBinaryTag stringTag) {
            return stringTag.value();
        } else if (tag instanceof IntArrayBinaryTag intArrayTag) {
            return intArrayTag.value();
        } else if (tag instanceof LongArrayBinaryTag longArrayTag) {
            return longArrayTag.value();
        } else {
            throw new UnsupportedOperationException("Unsupported NBT type: " + tag.getClass());
        }
    }

    private NBTUtils() {
    }
}
