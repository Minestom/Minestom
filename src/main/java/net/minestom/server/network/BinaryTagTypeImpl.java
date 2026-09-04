package net.minestom.server.network;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Map;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.DOUBLE;
import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.NetworkBuffer.FixedRawBytes;
import static net.minestom.server.network.NetworkBuffer.FixedRawInts;
import static net.minestom.server.network.NetworkBuffer.FixedRawLongs;
import static net.minestom.server.network.NetworkBuffer.INT;
import static net.minestom.server.network.NetworkBuffer.LONG;
import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.RAW_INTS;
import static net.minestom.server.network.NetworkBuffer.RAW_LONGS;
import static net.minestom.server.network.NetworkBuffer.SHORT;
import static net.minestom.server.network.NetworkBuffer.STRING_IO_UTF8;

@ApiStatus.Internal
record BinaryTagTypeImpl() implements NetworkBufferTypeImpl<BinaryTag> {
    static final NetworkBuffer.Type<BinaryTag> INSTANCE = new BinaryTagTypeImpl();
    static final NetworkBuffer.Type<CompoundBinaryTag> INSTANCE_COMPOUND = new CompoundType();

    static final byte TAG_END = 0;
    static final byte TAG_BYTE = 1;
    static final byte TAG_SHORT = 2;
    static final byte TAG_INT = 3;
    static final byte TAG_LONG = 4;
    static final byte TAG_FLOAT = 5;
    static final byte TAG_DOUBLE = 6;
    static final byte TAG_BYTE_ARRAY = 7;
    static final byte TAG_STRING = 8;
    static final byte TAG_LIST = 9;
    static final byte TAG_COMPOUND = 10;
    static final byte TAG_INT_ARRAY = 11;
    static final byte TAG_LONG_ARRAY = 12;

    @Override
    public void write(NetworkBuffer buffer, BinaryTag value) {
        buffer.write(BYTE, value.type().id());
        writePayload(buffer, value, 1);
    }

    static void writeNamed(NetworkBuffer buffer, String name, BinaryTag value) {
        buffer.write(BYTE, value.type().id());
        buffer.write(STRING_IO_UTF8, name);
        writePayload(buffer, value, 1);
    }

    @Override
    public BinaryTag read(NetworkBuffer buffer) {
        return readPayload(buffer, buffer.read(BYTE), 1);
    }

    private static void writePayload(NetworkBuffer buffer, BinaryTag tag, int depth) {
        switch (tag) {
            case CompoundBinaryTag value -> {
                if (depth > ServerProperties.NBT_MAX_DEPTH.get()) throw new IllegalArgumentException("NBT is nested too deeply (max: " + ServerProperties.NBT_MAX_DEPTH.get() + ")");
                for (Map.Entry<String, ? extends BinaryTag> entry : value) {
                    final BinaryTag child = entry.getValue();
                    buffer.write(BYTE, child.type().id());
                    buffer.write(STRING_IO_UTF8, entry.getKey());
                    writePayload(buffer, child, depth + 1);
                }
                buffer.write(BYTE, TAG_END);
            }
            case ListBinaryTag value -> {
                if (depth > ServerProperties.NBT_MAX_DEPTH.get()) throw new IllegalArgumentException("NBT is nested too deeply (max: " + ServerProperties.NBT_MAX_DEPTH.get() + ")");
                // A list encodes a single element type, so a heterogeneous one is boxed into a list of compounds
                final ListBinaryTag list = value.wrapHeterogeneity();
                buffer.write(BYTE, list.elementType().id());
                buffer.write(INT, list.size());
                for (BinaryTag entry : list) writePayload(buffer, entry, depth + 1);
            }
            case StringBinaryTag value -> buffer.write(STRING_IO_UTF8, value.value());
            case IntBinaryTag value -> buffer.write(INT, value.value());
            case ByteBinaryTag value -> buffer.write(BYTE, value.value());
            case ShortBinaryTag value -> buffer.write(SHORT, value.value());
            case LongBinaryTag value -> buffer.write(LONG, value.value());
            case FloatBinaryTag value -> buffer.write(FLOAT, value.value());
            case DoubleBinaryTag value -> buffer.write(DOUBLE, value.value());
            case ByteArrayBinaryTag value -> {
                final byte[] array = value.value();
                buffer.write(INT, array.length);
                buffer.write(RAW_BYTES, array);
            }
            case IntArrayBinaryTag value -> writeIntArray(buffer, value.value());
            case LongArrayBinaryTag value -> writeLongArray(buffer, value.value());
            case EndBinaryTag _ -> {
                // No payload
            }
        }
    }

    private static BinaryTag readPayload(NetworkBuffer buffer, byte type, int depth) {
        return switch (type) {
            case TAG_END -> EndBinaryTag.endBinaryTag();
            case TAG_BYTE -> ByteBinaryTag.byteBinaryTag(buffer.read(BYTE));
            case TAG_SHORT -> ShortBinaryTag.shortBinaryTag(buffer.read(SHORT));
            case TAG_INT -> IntBinaryTag.intBinaryTag(buffer.read(INT));
            case TAG_LONG -> LongBinaryTag.longBinaryTag(buffer.read(LONG));
            case TAG_FLOAT -> FloatBinaryTag.floatBinaryTag(buffer.read(FLOAT));
            case TAG_DOUBLE -> DoubleBinaryTag.doubleBinaryTag(buffer.read(DOUBLE));
            case TAG_STRING -> StringBinaryTag.stringBinaryTag(buffer.read(STRING_IO_UTF8));
            case TAG_BYTE_ARRAY -> ByteArrayBinaryTag.byteArrayBinaryTag(readByteArray(buffer));
            case TAG_INT_ARRAY -> IntArrayBinaryTag.intArrayBinaryTag(readIntArray(buffer));
            case TAG_LONG_ARRAY -> LongArrayBinaryTag.longArrayBinaryTag(readLongArray(buffer));
            case TAG_COMPOUND -> readCompound(buffer, depth);
            case TAG_LIST -> readList(buffer, depth);
            default -> throw new IllegalArgumentException("Invalid NBT type id: " + type);
        };
    }

    private static CompoundBinaryTag readCompound(NetworkBuffer buffer, int depth) {
        if (depth > ServerProperties.NBT_MAX_DEPTH.get()) throw new IllegalArgumentException("NBT is nested too deeply (max: " + ServerProperties.NBT_MAX_DEPTH.get() + ")");
        final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        while (true) {
            final byte type = buffer.read(BYTE);
            if (type == TAG_END) return builder.build();
            final String name = buffer.read(STRING_IO_UTF8);
            builder.put(name, readPayload(buffer, type, depth + 1));
        }
    }

    private static ListBinaryTag readList(NetworkBuffer buffer, int depth) {
        if (depth > ServerProperties.NBT_MAX_DEPTH.get()) throw new IllegalArgumentException("NBT is nested too deeply (max: " + ServerProperties.NBT_MAX_DEPTH.get() + ")");
        final byte elementType = buffer.read(BYTE);
        final int size = buffer.read(INT);
        if (size == 0) return ListBinaryTag.empty(); // An empty list has no element type
        Check.argCondition(elementType == TAG_END, "NBT list of TAG_End must be empty (size: {0})", size);
        // Every entry takes at least minimumSize bytes, so a negative or larger size cannot be read
        buffer.ensureReadable((long) size * minimumSize(elementType));
        final BinaryTag[] entries = new BinaryTag[size];
        for (int i = 0; i < size; i++) entries[i] = readPayload(buffer, elementType, depth + 1);
        // Every entry was read as elementType, so the first one carries it
        return ListBinaryTag.listBinaryTag(entries[0].type(), Arrays.asList(entries));
    }

    private static byte[] readByteArray(NetworkBuffer buffer) {
        final int length = readArrayLength(buffer, Byte.BYTES);
        return buffer.read(FixedRawBytes(length));
    }

    private static int[] readIntArray(NetworkBuffer buffer) {
        final int length = readArrayLength(buffer, Integer.BYTES);
        return buffer.read(FixedRawInts(length));
    }

    private static long[] readLongArray(NetworkBuffer buffer) {
        final int length = readArrayLength(buffer, Long.BYTES);
        return buffer.read(FixedRawLongs(length));
    }

    private static void writeIntArray(NetworkBuffer buffer, int[] array) {
        buffer.write(INT, array.length);
        buffer.write(RAW_INTS, array);
    }

    private static void writeLongArray(NetworkBuffer buffer, long[] array) {
        buffer.write(INT, array.length);
        buffer.write(RAW_LONGS, array);
    }

    private static int readArrayLength(NetworkBuffer buffer, int elementSize) {
        final int length = buffer.read(INT);
        buffer.ensureReadable((long) length * elementSize);
        return length;
    }

    private static int minimumSize(byte type) {
        return switch (type) {
            case TAG_BYTE -> Byte.BYTES;
            case TAG_SHORT -> Short.BYTES;
            case TAG_INT, TAG_FLOAT -> Integer.BYTES;
            case TAG_LONG, TAG_DOUBLE -> Long.BYTES;
            case TAG_STRING -> Short.BYTES; // Length prefix of an empty string
            case TAG_BYTE_ARRAY, TAG_INT_ARRAY, TAG_LONG_ARRAY -> Integer.BYTES; // Length prefix of an empty array
            case TAG_LIST -> Byte.BYTES + Integer.BYTES; // Element type and length prefix of an empty list
            case TAG_COMPOUND -> Byte.BYTES; // Trailing TAG_End
            default -> throw new IllegalArgumentException("Invalid NBT type id: " + type);
        };
    }

    private record CompoundType() implements NetworkBufferTypeImpl<CompoundBinaryTag> {
        @Override
        public void write(NetworkBuffer buffer, CompoundBinaryTag value) {
            buffer.write(BYTE, TAG_COMPOUND);
            writePayload(buffer, value, 1);
        }

        @Override
        public CompoundBinaryTag read(NetworkBuffer buffer) {
            final byte type = buffer.read(BYTE);
            Check.argCondition(type != TAG_COMPOUND, "Binary tag is not a compound: {0}", type);
            return readCompound(buffer, 1);
        }
    }
}
