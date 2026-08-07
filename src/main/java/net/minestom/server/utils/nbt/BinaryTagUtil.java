package net.minestom.server.utils.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class BinaryTagUtil {
    private static final BinaryTagType<?>[] TYPES = new BinaryTagType<?>[]{
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

    public static BinaryTagType<?> nbtTypeFromId(byte id) {
        Check.argCondition(id < 0 || id >= TYPES.length, "Invalid NBT type id: " + id);
        return TYPES[id];
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
