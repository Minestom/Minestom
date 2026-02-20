package net.minestom.server.utils.nbt;

import net.kyori.adventure.nbt.*;
import org.jetbrains.annotations.ApiStatus;

import static net.kyori.adventure.nbt.BinaryTagTypes.*;

@ApiStatus.Internal
public final class BinaryTagUtil {
    public static BinaryTagType<? extends BinaryTag> nbtTypeFromId(byte id) {
        return switch (id) {
            case 0 -> END;
            case 1 -> BYTE;
            case 2 -> SHORT;
            case 3 -> INT;
            case 4 -> LONG;
            case 5 -> FLOAT;
            case 6 -> DOUBLE;
            case 7 -> BYTE_ARRAY;
            case 8 -> STRING;
            case 9 -> LIST;
            case 10 -> COMPOUND;
            case 11 -> INT_ARRAY;
            case 12 -> LONG_ARRAY;
            default -> throw new IllegalArgumentException("Invalid NBT type id: " + id);
        };
    }

    public static Object nbtValueFromTag(BinaryTag tag) {
        return switch (tag) {
            case ByteBinaryTag byteTag -> byteTag.value();
            case ShortBinaryTag shortTag -> shortTag.value();
            case IntBinaryTag intTag -> intTag.value();
            case LongBinaryTag longTag -> longTag.value();
            case FloatBinaryTag floatTag -> floatTag.value();
            case DoubleBinaryTag doubleTag -> doubleTag.value();
            case ByteArrayBinaryTag byteArrayTag -> byteArrayTag.value();
            case StringBinaryTag stringTag -> stringTag.value();
            case IntArrayBinaryTag intArrayTag -> intArrayTag.value();
            case LongArrayBinaryTag longArrayTag -> longArrayTag.value();
            default -> throw new UnsupportedOperationException("Unsupported NBT type: " + tag.getClass());
        };
    }

    private BinaryTagUtil() {
    }
}
