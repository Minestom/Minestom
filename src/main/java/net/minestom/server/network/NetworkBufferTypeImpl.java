package net.minestom.server.network;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.template.PrimitiveType;
import net.minestom.server.network.template.TransformingType;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.json.JsonUtil;
import net.minestom.server.utils.nbt.BinaryTagReader;
import net.minestom.server.utils.nbt.BinaryTagWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

final class NetworkBufferTypeImpl {
    static final int SEGMENT_BITS = 0x7F;
    static final int CONTINUE_BIT = 0x80;

    static final BooleanType BOOLEAN = (BooleanType) NetworkBuffer.BOOLEAN;
    static final ByteType BYTE = (ByteType) NetworkBuffer.BYTE;
    static final UnsignedByteType UNSIGNED_BYTE = (UnsignedByteType) NetworkBuffer.UNSIGNED_BYTE;
    static final UnsignedShortType UNSIGNED_SHORT = (UnsignedShortType) NetworkBuffer.UNSIGNED_SHORT;
    static final IntType INT = (IntType) NetworkBuffer.INT;
    static final UnsignedIntType UNSIGNED_INT = (UnsignedIntType) NetworkBuffer.UNSIGNED_INT;
    static final FloatType FLOAT = (FloatType) NetworkBuffer.FLOAT;
    static final DoubleType DOUBLE = (DoubleType) NetworkBuffer.DOUBLE;
    static final VarIntType VAR_INT = (VarIntType) NetworkBuffer.VAR_INT;
    static final LongType LONG = (LongType) NetworkBuffer.LONG;
    static final VarLongType VAR_LONG = (VarLongType) NetworkBuffer.VAR_LONG;

    record BooleanType() implements PrimitiveType<Boolean> {
        @Override
        public void write(NetworkBuffer buffer, Boolean value) {
            writeBoolean(buffer, value);
        }

        @Override
        public Boolean read(NetworkBuffer buffer) {
            return readBoolean(buffer);
        }

        @Override
        public long sizeOf(Boolean value, @Nullable Registries registries) {
            return Byte.BYTES;
        }

        @Override
        public Class<Boolean> primitiveClass() {
            return boolean.class;
        }

        @Override
        public void writeBoolean(NetworkBuffer buffer, boolean value) {
            buffer.ensureWritable(Byte.BYTES);
            buffer.direct().putByte(buffer.writeIndex(), value ? (byte) 1 : (byte) 0);
            buffer.advanceWrite(Byte.BYTES);
        }

        @Override
        public boolean readBoolean(NetworkBuffer buffer) {
            buffer.ensureReadable(Byte.BYTES);
            final byte value = buffer.direct().getByte(buffer.readIndex());
            buffer.advanceRead(Byte.BYTES);
            return value == 1;
        }
    }

    record ByteType() implements PrimitiveType<Byte> {
        @Override
        public void write(NetworkBuffer buffer, Byte value) {
            writeByte(buffer, value);
        }

        @Override
        public Byte read(NetworkBuffer buffer) {
            return readByte(buffer);
        }

        @Override
        public long sizeOf(Byte value, @Nullable Registries registries) {
            return Byte.BYTES;
        }

        @Override
        public Class<Byte> primitiveClass() {
            return byte.class;
        }

        @Override
        public void writeByte(NetworkBuffer buffer, byte value) {
            buffer.ensureWritable(Byte.BYTES);
            buffer.direct().putByte(buffer.writeIndex(), value);
            buffer.advanceWrite(Byte.BYTES);
        }

        @Override
        public byte readByte(NetworkBuffer buffer) {
            buffer.ensureReadable(Byte.BYTES);
            final byte value = buffer.direct().getByte(buffer.readIndex());
            buffer.advanceRead(Byte.BYTES);
            return value;
        }
    }

    record UnsignedByteType() implements PrimitiveType<Short>, PrimitiveType.Unsigned {
        @Override
        public void write(NetworkBuffer buffer, Short value) {
            writeShort(buffer, value);
        }

        @Override
        public Short read(NetworkBuffer buffer) {
            return readShort(buffer);
        }

        @Override
        public long sizeOf(Short value, @Nullable Registries registries) {
            return Byte.BYTES;
        }

        @Override
        public Class<Short> primitiveClass() {
            return short.class;
        }

        @Override
        public void writeShort(NetworkBuffer buffer, short value) {
            buffer.ensureWritable(Byte.BYTES);
            buffer.direct().putByte(buffer.writeIndex(), (byte) (value & 0xFF));
            buffer.advanceWrite(Byte.BYTES);
        }

        @Override
        public short readShort(NetworkBuffer buffer) {
            buffer.ensureReadable(Byte.BYTES);
            final byte value = buffer.direct().getByte(buffer.readIndex());
            buffer.advanceRead(Byte.BYTES);
            return (short) (value & 0xFF);
        }
    }

    record ShortType() implements PrimitiveType<Short> {
        @Override
        public void write(NetworkBuffer buffer, Short value) {
            writeShort(buffer, value);
        }

        @Override
        public Short read(NetworkBuffer buffer) {
            return readShort(buffer);
        }

        @Override
        public long sizeOf(Short value, @Nullable Registries registries) {
            return Short.BYTES;
        }

        @Override
        public Class<Short> primitiveClass() {
            return short.class;
        }

        @Override
        public void writeShort(NetworkBuffer buffer, short value) {
            buffer.ensureWritable(Short.BYTES);
            buffer.direct().putShort(buffer.writeIndex(), value);
            buffer.advanceWrite(Short.BYTES);
        }

        @Override
        public short readShort(NetworkBuffer buffer) {
            buffer.ensureReadable(Short.BYTES);
            final short value = buffer.direct().getShort(buffer.readIndex());
            buffer.advanceRead(Short.BYTES);
            return value;
        }
    }

    record UnsignedShortType() implements PrimitiveType<Integer>, PrimitiveType.Unsigned {
        @Override
        public void write(NetworkBuffer buffer, Integer value) {
            writeInt(buffer, value);
        }

        @Override
        public Integer read(NetworkBuffer buffer) {
            return readInt(buffer);
        }

        @Override
        public long sizeOf(Integer value, @Nullable Registries registries) {
            return Short.BYTES;
        }

        @Override
        public Class<Integer> primitiveClass() {
            return int.class;
        }

        @Override
        public void writeInt(NetworkBuffer buffer, int value) {
            buffer.ensureWritable(Short.BYTES);
            buffer.direct().putShort(buffer.writeIndex(), (short) (value & 0xFFFF));
            buffer.advanceWrite(Short.BYTES);
        }

        @Override
        public int readInt(NetworkBuffer buffer) {
            buffer.ensureReadable(Short.BYTES);
            final short value = buffer.direct().getShort(buffer.readIndex());
            buffer.advanceRead(Short.BYTES);
            return value & 0xFFFF;
        }
    }

    record IntType() implements PrimitiveType<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer value) {
            writeInt(buffer, value);
        }

        @Override
        public Integer read(NetworkBuffer buffer) {
            return readInt(buffer);
        }

        @Override
        public long sizeOf(Integer value, @Nullable Registries registries) {
            return Integer.BYTES;
        }

        @Override
        public Class<Integer> primitiveClass() {
            return int.class;
        }

        @Override
        public void writeInt(NetworkBuffer buffer, int value) {
            buffer.ensureWritable(Integer.BYTES);
            buffer.direct().putInt(buffer.writeIndex(), value);
            buffer.advanceWrite(Integer.BYTES);
        }

        @Override
        public int readInt(NetworkBuffer buffer) {
            buffer.ensureReadable(Integer.BYTES);
            final int value = buffer.direct().getInt(buffer.readIndex());
            buffer.advanceRead(Integer.BYTES);
            return value;
        }
    }

    record UnsignedIntType() implements PrimitiveType<Long>, PrimitiveType.Unsigned {
        @Override
        public void write(NetworkBuffer buffer, Long value) {
            writeLong(buffer, value);
        }

        @Override
        public Long read(NetworkBuffer buffer) {
            return readLong(buffer);
        }

        @Override
        public long sizeOf(Long value, @Nullable Registries registries) {
            return Integer.BYTES;
        }

        @Override
        public Class<Long> primitiveClass() {
            return long.class;
        }

        @Override
        public void writeLong(NetworkBuffer buffer, long value) {
            buffer.ensureWritable(Integer.BYTES);
            buffer.direct().putInt(buffer.writeIndex(), (int) (value & 0xFFFFFFFFL));
            buffer.advanceWrite(Integer.BYTES);
        }

        @Override
        public long readLong(NetworkBuffer buffer) {
            buffer.ensureReadable(Integer.BYTES);
            final int value = buffer.direct().getInt(buffer.readIndex());
            buffer.advanceRead(Integer.BYTES);
            return value & 0xFFFFFFFFL;
        }
    }

    record LongType() implements PrimitiveType<Long> {
        @Override
        public void write(NetworkBuffer buffer, Long value) {
            writeLong(buffer, value);
        }

        @Override
        public Long read(NetworkBuffer buffer) {
            return readLong(buffer);
        }

        @Override
        public long sizeOf(Long value, @Nullable Registries registries) {
            return Long.BYTES;
        }

        @Override
        public Class<Long> primitiveClass() {
            return long.class;
        }

        @Override
        public void writeLong(NetworkBuffer buffer, long value) {
            buffer.ensureWritable(Long.BYTES);
            buffer.direct().putLong(buffer.writeIndex(), value);
            buffer.advanceWrite(Long.BYTES);
        }

        @Override
        public long readLong(NetworkBuffer buffer) {
            buffer.ensureReadable(Long.BYTES);
            final long value = buffer.direct().getLong(buffer.readIndex());
            buffer.advanceRead(Long.BYTES);
            return value;
        }
    }

    record FloatType() implements PrimitiveType<Float> {
        @Override
        public void write(NetworkBuffer buffer, Float value) {
            writeFloat(buffer, value);
        }

        @Override
        public Float read(NetworkBuffer buffer) {
            return readFloat(buffer);
        }

        @Override
        public long sizeOf(Float value, @Nullable Registries registries) {
            return Float.BYTES;
        }

        @Override
        public Class<Float> primitiveClass() {
            return float.class;
        }

        @Override
        public void writeFloat(NetworkBuffer buffer, float value) {
            buffer.ensureWritable(Float.BYTES);
            buffer.direct().putFloat(buffer.writeIndex(), value);
            buffer.advanceWrite(Float.BYTES);
        }

        @Override
        public float readFloat(NetworkBuffer buffer) {
            buffer.ensureReadable(Float.BYTES);
            final float value = buffer.direct().getFloat(buffer.readIndex());
            buffer.advanceRead(Float.BYTES);
            return value;
        }
    }

    record DoubleType() implements PrimitiveType<Double> {
        @Override
        public void write(NetworkBuffer buffer, Double value) {
            writeDouble(buffer, value);
        }

        @Override
        public Double read(NetworkBuffer buffer) {
            return readDouble(buffer);
        }

        @Override
        public long sizeOf(Double value, @Nullable Registries registries) {
            return Double.BYTES;
        }

        @Override
        public Class<Double> primitiveClass() {
            return double.class;
        }

        @Override
        public void writeDouble(NetworkBuffer buffer, double value) {
            buffer.ensureWritable(Double.BYTES);
            buffer.direct().putDouble(buffer.writeIndex(), value);
            buffer.advanceWrite(Double.BYTES);
        }

        @Override
        public double readDouble(NetworkBuffer buffer) {
            buffer.ensureReadable(Double.BYTES);
            final double value = buffer.direct().getDouble(buffer.readIndex());
            buffer.advanceRead(Double.BYTES);
            return value;
        }
    }

    record VarIntType() implements PrimitiveType<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer boxed) {
            writeInt(buffer, boxed);
        }

        @Override
        public Integer read(NetworkBuffer buffer) {
            return readInt(buffer);
        }

        int sizeOf(int value) {
            int normal = value | -(value >>> 31) | 1;
            int bits = 32 - Integer.numberOfLeadingZeros(normal);
            return (bits + 6) / 7;
        }

        @Override
        public long sizeOf(Integer value, @Nullable Registries registries) {
            return sizeOf(value.intValue());
        }

        @Override
        public Class<Integer> primitiveClass() {
            return int.class;
        }

        @Override
        public void writeInt(NetworkBuffer buffer, int value) {
            if (buffer.writableBytes() < 5) {
                buffer.ensureWritable(sizeOf(value));
            }
            long index = buffer.writeIndex();
            var nio = buffer.direct();
            for (long i = 0; i < 4; i++) { // Using a counted loop allows easier unrolling.
                if ((value & ~SEGMENT_BITS) == 0) {
                    break;
                }
                nio.putByte(index++, (byte) (value & SEGMENT_BITS | CONTINUE_BIT));
                // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
                value >>>= 7;
            }
            nio.putByte(index++, (byte) value);
            buffer.advanceWrite(index - buffer.writeIndex());
        }

        @Override
        public int readInt(NetworkBuffer buffer) {
            int result = 0;
            for (int i = 0; i < 5; i++) {
                byte b = BYTE.readByte(buffer);
                result |= (b & SEGMENT_BITS) << (i * 7);
                if (b >= 0) {
                    return result;
                }
            }
            throw new IllegalStateException("VarInt is too big");
        }
    }

    record OptionalVarIntType() implements Type<@Nullable Integer> {
        @Override
        public void write(NetworkBuffer buffer, @Nullable Integer value) {
            VAR_INT.writeInt(buffer, value == null ? 0 : value + 1);
        }

        @Override
        public @Nullable Integer read(NetworkBuffer buffer) {
            final int value = VAR_INT.readInt(buffer);
            return value == 0 ? null : value - 1;
        }

        @Override
        public long sizeOf(@Nullable Integer value) {
            return VAR_INT.sizeOf(value == null ? 0 : value + 1);
        }
    }

    record VarInt3Type() implements Type<Integer>, PrimitiveType<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer boxed) {
            writeInt(buffer, boxed);
        }

        @Override
        public Integer read(NetworkBuffer buffer) {
            return readInt(buffer);
        }

        @Override
        public Class<Integer> primitiveClass() {
            return int.class;
        }

        @Override
        public void writeInt(NetworkBuffer buffer, int value) {
            // Value must be between 0 and 2^21
            Check.argCondition(value < 0 || value >= (1 << 21), "VarInt3 out of bounds: {0}", value);
            buffer.ensureWritable(3);
            final long startIndex = buffer.writeIndex();
            var impl = buffer.direct();
            impl.putByte(startIndex, (byte) ((value & SEGMENT_BITS) | CONTINUE_BIT));
            impl.putByte(startIndex + 1, (byte) (((value >>> 7) & SEGMENT_BITS) | CONTINUE_BIT));
            impl.putByte(startIndex + 2, (byte) (value >>> 14));
            buffer.advanceWrite(3);
        }

        @Override
        public int readInt(NetworkBuffer buffer) {
            return VAR_INT.readInt(buffer);
        }
    }

    record VarLongType() implements PrimitiveType<Long> {
        @Override
        public void write(NetworkBuffer buffer, Long value) {
            writeLong(buffer, value);
        }

        @Override
        public Long read(NetworkBuffer buffer) {
            return readLong(buffer);
        }

        public long sizeOf(long value) {
            long normal = value | -(value >>> 63) | 1;
            int bits = 64 - Long.numberOfLeadingZeros(normal);
            return (bits + 6) / 7;
        }

        @Override
        public long sizeOf(Long value, @Nullable Registries registries) {
            return sizeOf(value.longValue());
        }

        @Override
        public Class<Long> primitiveClass() {
            return long.class;
        }

        @Override
        public void writeLong(NetworkBuffer buffer, long value) {
            buffer.ensureWritable(10);
            int size = 0;
            while (true) {
                if ((value & ~((long) SEGMENT_BITS)) == 0) {
                    buffer.direct().putByte(buffer.writeIndex() + size, (byte) value);
                    buffer.advanceWrite(size + 1);
                    return;
                }
                buffer.direct().putByte(buffer.writeIndex() + size, (byte) (value & SEGMENT_BITS | CONTINUE_BIT));
                size++;
                // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
                value >>>= 7;
            }
        }

        @Override
        public long readLong(NetworkBuffer buffer) {
            int length = 0;
            long value = 0;
            int position = 0;
            byte currentByte;
            while (true) {
                currentByte = buffer.direct().getByte(buffer.readIndex() + length);
                length++;
                value |= (long) (currentByte & SEGMENT_BITS) << position;
                if ((currentByte & CONTINUE_BIT) == 0) break;
                position += 7;
                if (position >= 64) throw new IllegalStateException("VarLong is too big");
            }
            buffer.advanceRead(length);
            return value;
        }
    }

    record RawBytesType(int length) implements Type<byte[]> {
        @Override
        public void write(NetworkBuffer buffer, byte[] value) {
            if (length != -1 && value.length != length) {
                throw new IllegalArgumentException("Invalid length: " + value.length + " != " + length);
            }
            final int length = value.length;
            if (length == 0) return;
            buffer.ensureWritable(length);
            buffer.direct().putBytes(buffer.writeIndex(), value);
            buffer.advanceWrite(length);
        }

        @Override
        public byte[] read(NetworkBuffer buffer) {
            long length = this.length;
            if (this.length == -1) {
                length = buffer.readableBytes();
            }
            if (length == 0) return new byte[0];
            buffer.ensureReadable(length);
            final int arrayLength = Math.toIntExact(length);
            final byte[] bytes = new byte[arrayLength];
            buffer.direct().getBytes(buffer.readIndex(), bytes);
            buffer.advanceRead(arrayLength);
            return bytes;
        }

        @Override
        public long sizeOf(byte[] value, @Nullable Registries registries) {
            return length;
        }
    }

    record StringType() implements Type<String> {
        @Override
        public void write(NetworkBuffer buffer, String value) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            buffer.write(BYTE_ARRAY, bytes);
        }

        @Override
        public String read(NetworkBuffer buffer) {
            final int length = VAR_INT.readInt(buffer);
            buffer.ensureReadable(length);
            String string = buffer.direct().getString(buffer.readIndex(), length);
            buffer.advanceRead(length);
            return string;
        }
    }

    record StringTerminatedType() implements Type<String> {
        @Override
        public void write(NetworkBuffer buffer, String value) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            final int length = bytes.length + 1;
            buffer.ensureWritable(length);
            var impl = buffer.direct();
            impl.putBytes(buffer.writeIndex(), bytes);
            impl.putByte(buffer.writeIndex() + bytes.length, (byte) 0); // null terminator
            buffer.advanceWrite(length);
        }

        @Override
        public String read(NetworkBuffer buffer) {
            ByteArrayList bytes = new ByteArrayList();
            byte b;
            while ((b = BYTE.readByte(buffer)) != 0) {
                bytes.add(b);
            }
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    record NbtType() implements Type<BinaryTag> {
        static final NbtType TYPE = new NbtType();

        @SuppressWarnings("unchecked")
        public static <T extends BinaryTag> Type<T> typed() {
            return (Type<T>) TYPE;
        }

        @Override
        public void write(NetworkBuffer buffer, BinaryTag value) {
            final BinaryTagWriter nbtWriter = new BinaryTagWriter(buffer.ioView());
            try {
                nbtWriter.writeNameless(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public BinaryTag read(NetworkBuffer buffer) {
            final BinaryTagReader nbtReader = new BinaryTagReader(buffer.ioView());
            try {
                return nbtReader.readNameless();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    record OptionalNBTType() implements Type<@Nullable BinaryTag> {
        static final OptionalNBTType INSTANCE = new OptionalNBTType();

        @SuppressWarnings("unchecked")
        static <T extends @Nullable BinaryTag> Type<T> typed() {
            return (Type<T>) INSTANCE;
        }

        @Override
        public void write(NetworkBuffer buffer, @Nullable BinaryTag value) {
            if (value != null) {
                NbtType.TYPE.write(buffer, value);
            } else {
                // TAG_END
                BYTE.writeByte(buffer, (byte) 0x00);
            }
        }

        @Override
        public @Nullable BinaryTag read(NetworkBuffer buffer) {
            var type = NbtType.TYPE.read(buffer);
            // TAG_END == null
            if (type == EndBinaryTag.endBinaryTag()) return null;
            return type;
        }
    }

    record BlockPositionType() implements Type<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            final int blockX = value.blockX();
            final int blockY = value.blockY();
            final int blockZ = value.blockZ();
            final long longPos = (((long) blockX & 0x3FFFFFF) << 38) |
                    (((long) blockZ & 0x3FFFFFF) << 12) |
                    ((long) blockY & 0xFFF);
            LONG.writeLong(buffer, longPos);
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            final long value = LONG.readLong(buffer);
            final int x = (int) (value >> 38);
            final int y = (int) (value << 52 >> 52);
            final int z = (int) (value << 26 >> 38);
            return new BlockVec(x, y, z);
        }
    }

    record JsonComponentType() implements Type<Component> {
        @Override
        public void write(NetworkBuffer buffer, Component value) {
            final Registries registries = buffer.registries();
            final Transcoder<JsonElement> coder = registries != null
                    ? new RegistryTranscoder<>(Transcoder.JSON, registries)
                    : Transcoder.JSON;
            final String json = JsonUtil.toJson(Codec.COMPONENT.encode(coder, value).orElseThrow());
            STRING.write(buffer, json);
        }

        @Override
        public Component read(NetworkBuffer buffer) {
            final Registries registries = buffer.registries();
            final Transcoder<JsonElement> coder = registries != null
                    ? new RegistryTranscoder<>(Transcoder.JSON, registries)
                    : Transcoder.JSON;
            final JsonElement json = JsonUtil.fromJson(STRING.read(buffer));
            return Codec.COMPONENT.decode(coder, json).orElseThrow();
        }
    }

    record UUIDType() implements Type<UUID> {
        @Override
        public void write(NetworkBuffer buffer, java.util.UUID value) {
            LONG.writeLong(buffer, value.getMostSignificantBits());
            LONG.writeLong(buffer, value.getLeastSignificantBits());
        }

        @Override
        public java.util.UUID read(NetworkBuffer buffer) {
            final long mostSignificantBits = LONG.readLong(buffer);
            final long leastSignificantBits = LONG.readLong(buffer);
            return new UUID(mostSignificantBits, leastSignificantBits);
        }
    }

    record PosType() implements Type<Pos> {
        @Override
        public void write(NetworkBuffer buffer, Pos value) {
            DOUBLE.writeDouble(buffer, value.x());
            DOUBLE.writeDouble(buffer, value.y());
            DOUBLE.writeDouble(buffer, value.z());
            FLOAT.writeFloat(buffer, value.yaw());
            FLOAT.writeFloat(buffer, value.pitch());
        }

        @Override
        public Pos read(NetworkBuffer buffer) {
            final double x = DOUBLE.readDouble(buffer);
            final double y = DOUBLE.readDouble(buffer);
            final double z = DOUBLE.readDouble(buffer);
            final float yaw = FLOAT.readFloat(buffer);
            final float pitch = FLOAT.readFloat(buffer);
            return new Pos(x, y, z, yaw, pitch);
        }
    }

    record ByteArrayType() implements Type<byte[]> {
        @Override
        public void write(NetworkBuffer buffer, byte[] value) {
            VAR_INT.writeInt(buffer, value.length);
            RAW_BYTES.write(buffer, value);
        }

        @Override
        public byte[] read(NetworkBuffer buffer) {
            final int length = VAR_INT.readInt(buffer);
            if (length == 0) return new byte[0];
            final long remaining = buffer.readableBytes();
            Check.argCondition(length > remaining, "String is too long (length: {0}, readable: {1})", length, remaining);
            return buffer.read(FixedRawBytes(length));
        }
    }

    record LongArrayType() implements Type<long[]> {
        @Override
        public void write(NetworkBuffer buffer, long[] value) {
            VAR_INT.writeInt(buffer, value.length);
            for (long l : value) LONG.writeLong(buffer, l);
        }

        @Override
        public long[] read(NetworkBuffer buffer) {
            final int length = VAR_INT.readInt(buffer);
            final long[] longs = new long[length];
            for (int i = 0; i < length; i++) longs[i] = LONG.readLong(buffer);
            return longs;
        }
    }

    record VarIntArrayType() implements Type<int[]> {
        @Override
        public void write(NetworkBuffer buffer, int[] value) {
            VAR_INT.write(buffer, value.length);
            for (int i : value) VAR_INT.write(buffer, i);
        }

        @Override
        public int[] read(NetworkBuffer buffer) {
            final int length = VAR_INT.readInt(buffer);
            final int[] ints = new int[length];
            for (int i = 0; i < length; i++) ints[i] = VAR_INT.readInt(buffer);
            return ints;
        }
    }

    record VarLongArrayType() implements Type<long[]> {
        @Override
        public void write(NetworkBuffer buffer, long[] value) {
            VAR_INT.writeInt(buffer, value.length);
            for (long l : value) VAR_LONG.writeLong(buffer, l);
        }

        @Override
        public long[] read(NetworkBuffer buffer) {
            final int length = VAR_INT.readInt(buffer);
            final long[] longs = new long[length];
            for (int i = 0; i < length; i++) longs[i] = VAR_LONG.readLong(buffer);
            return longs;
        }
    }

    record Vector3Type() implements Type<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            FLOAT.writeFloat(buffer, (float) value.x());
            FLOAT.writeFloat(buffer, (float) value.y());
            FLOAT.writeFloat(buffer, (float) value.z());
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            final float x = FLOAT.readFloat(buffer);
            final float y = FLOAT.readFloat(buffer);
            final float z = FLOAT.readFloat(buffer);
            return new Vec(x, y, z);
        }
    }

    record Vector3DType() implements Type<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            DOUBLE.writeDouble(buffer, value.x());
            DOUBLE.writeDouble(buffer, value.y());
            DOUBLE.writeDouble(buffer, value.z());
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            final double x = DOUBLE.readDouble(buffer);
            final double y = DOUBLE.readDouble(buffer);
            final double z = DOUBLE.readDouble(buffer);
            return new Vec(x, y, z);
        }
    }

    record Vector3IType() implements Type<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            VAR_INT.writeInt(buffer, (int) value.x());
            VAR_INT.writeInt(buffer, (int) value.y());
            VAR_INT.writeInt(buffer, (int) value.z());
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            final int x = VAR_INT.readInt(buffer);
            final int y = VAR_INT.readInt(buffer);
            final int z = VAR_INT.readInt(buffer);
            return new BlockVec(x, y, z);
        }
    }

    record Vector3BType() implements Type<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            BYTE.writeByte(buffer, (byte) value.x());
            BYTE.writeByte(buffer, (byte) value.y());
            BYTE.writeByte(buffer, (byte) value.z());
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            final byte x = BYTE.readByte(buffer);
            final byte y = BYTE.readByte(buffer);
            final byte z = BYTE.readByte(buffer);
            return new BlockVec(x, y, z);
        }
    }

    record LpVector3Type() implements Type<Vec> {
        private static final int DATA_BITS_MASK = 0b111111111111111;
        private static final double MAX_QUANTIZED_VALUE = 32766.0;
        private static final int SCALE_BITS_MASK = 0b11;
        private static final int CONTINUATION_FLAG = 4;
        private static final int X_OFFSET = 3;
        private static final int Y_OFFSET = 18;
        private static final int Z_OFFSET = 33;
        public static final double ABS_MAX_VALUE = 1.7179869183E10;
        public static final double ABS_MIN_VALUE = 3.051944088384301E-5;

        @Override
        public void write(NetworkBuffer buffer, Vec value) {
            double x = sanitize(value.x()), y = sanitize(value.y()), z = sanitize(value.z());
            double max = MathUtils.absMax(x, MathUtils.absMax(y, z));
            if (max < ABS_MIN_VALUE) {
                BYTE.writeByte(buffer, (byte) 0);
            } else {
                long i = MathUtils.ceilLong(max);
                boolean hasContinuation = (i & SCALE_BITS_MASK) != i;
                long flags = hasContinuation ? i & SCALE_BITS_MASK | CONTINUATION_FLAG : i;
                long px = pack(x / i) << X_OFFSET;
                long py = pack(y / i) << Y_OFFSET;
                long pz = pack(z / i) << Z_OFFSET;
                long packed = flags | px | py | pz;
                BYTE.writeByte(buffer, (byte) packed);
                BYTE.writeByte(buffer, (byte) (packed >> 8));
                INT.writeInt(buffer, (int) (packed >> 16));
                if (hasContinuation)
                    VAR_INT.writeInt(buffer, (int) (i >> 2));
            }
        }

        @Override
        public Vec read(NetworkBuffer buffer) {
            short flags = UNSIGNED_BYTE.readShort(buffer);
            if (flags == 0) {
                return Vec.ZERO;
            } else {
                short p2 = UNSIGNED_BYTE.readShort(buffer);
                long p3 = UNSIGNED_INT.readLong(buffer);
                long value = p3 << 16 | p2 << 8 | flags;
                long scale = flags & SCALE_BITS_MASK;
                if ((flags & CONTINUATION_FLAG) == CONTINUATION_FLAG)
                    scale |= (VAR_INT.readInt(buffer) & 0xFFFFFFFFL) << 2;
                return new Vec(
                        unpack(value >> X_OFFSET) * scale,
                        unpack(value >> Y_OFFSET) * scale,
                        unpack(value >> Z_OFFSET) * scale
                );
            }
        }

        private static double sanitize(double value) {
            return Double.isNaN(value) ? 0.0 : Math.clamp(value, -ABS_MAX_VALUE, ABS_MAX_VALUE);
        }

        private static long pack(double value) {
            return Math.round((value * 0.5 + 0.5) * MAX_QUANTIZED_VALUE);
        }

        private static double unpack(long value) {
            return Math.min((double) (value & DATA_BITS_MASK), MAX_QUANTIZED_VALUE) * 2.0 / MAX_QUANTIZED_VALUE - 1.0;
        }
    }

    record QuaternionType() implements Type<float[]> {
        @Override
        public void write(NetworkBuffer buffer, float[] value) {
            FLOAT.writeFloat(buffer, value[0]);
            FLOAT.writeFloat(buffer, value[1]);
            FLOAT.writeFloat(buffer, value[2]);
            FLOAT.writeFloat(buffer, value[3]);
        }

        @Override
        public float[] read(NetworkBuffer buffer) {
            final float x = FLOAT.readFloat(buffer);
            final float y = FLOAT.readFloat(buffer);
            final float z = FLOAT.readFloat(buffer);
            final float w = FLOAT.readFloat(buffer);
            return new float[]{x, y, z, w};
        }
    }

    // Combinators

    record EnumSetType<E extends Enum<E>>(Class<E> enumType,
                                          E[] values, Type<BitSet> bitSetType) implements Type<EnumSet<E>> {
        public EnumSetType {
            Objects.requireNonNull(enumType, "enumType");
            Objects.requireNonNull(values, "values");
            Objects.requireNonNull(bitSetType, "bitSetType");
        }

        @Override
        public void write(NetworkBuffer buffer, EnumSet<E> value) {
            BitSet bitSet = new BitSet(values.length);
            for (int i = 0; i < values.length; ++i) {
                bitSet.set(i, value.contains(values[i]));
            }
            bitSetType.write(buffer, bitSet);
        }

        @Override
        public EnumSet<E> read(NetworkBuffer buffer) {
            final BitSet bitSet = bitSetType.read(buffer);
            EnumSet<E> enumSet = EnumSet.noneOf(enumType);
            for (int i = 0; i < values.length; ++i) {
                if (bitSet.get(i)) {
                    enumSet.add(values[i]);
                }
            }
            return enumSet;
        }
    }

    record FixedBitSetType(int length, Type<byte[]> arrayType) implements Type<BitSet> {
        public FixedBitSetType {
            Check.argCondition(length < 0, "Length is negative found {0}", length);
            Objects.requireNonNull(arrayType, "arrayType");
        }

        @Override
        public void write(NetworkBuffer buffer, BitSet value) {
            if (value.length() > length) {
                throw new IllegalArgumentException("BitSet is larger than expected size (" + value.length() + ">" + length + ")");
            }
            byte[] array = value.toByteArray();
            final int length = (this.length + 7) / Long.BYTES;
            if (array.length != length) {
                array = Arrays.copyOf(array, length);
            }
            arrayType.write(buffer, array);
        }

        @Override
        public BitSet read(NetworkBuffer buffer) {
            final byte[] array = arrayType.read(buffer);
            return BitSet.valueOf(array);
        }
    }

    record OptionalType<T extends @Nullable Object>(Type<T> parent) implements Type<T> {
        public OptionalType {
            Objects.requireNonNull(parent, "parent");
        }

        // We flip the normal way to promote inlining.
        @Override
        public void write(NetworkBuffer buffer, @Nullable T value) {
            if (value == null) {
                BOOLEAN.writeBoolean(buffer, false);
                return;
            }
            BOOLEAN.writeBoolean(buffer, true);
            parent.write(buffer, value);
        }

        @Override
        public @Nullable T read(NetworkBuffer buffer) {
            return BOOLEAN.readBoolean(buffer) ? parent.read(buffer) : null;
        }

        @Override
        public long sizeOf(T value, @Nullable Registries registries) {
            if (value == null) return BOOLEAN.sizeOf(false, registries);
            return BOOLEAN.sizeOf(true, registries) + parent.sizeOf(value, registries);
        }
    }

    record LengthPrefixedType<T>(Type<T> parent, int maxLength) implements Type<T> {
        public LengthPrefixedType {
            Objects.requireNonNull(parent, "parent");
            Check.argCondition(maxLength < 0, "length is negative found {0}", maxLength);
        }

        @Override
        public void write(NetworkBuffer buffer, T value) {
            // Write to another buffer and copy (kinda inefficient, but currently unused serverside so its ok for now)
            final byte[] componentData = NetworkBuffer.makeArray(parent, value, buffer.registries());
            BYTE_ARRAY.write(buffer, componentData);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            final int length = VAR_INT.readInt(buffer);
            Check.argCondition(length > maxLength, "Value is too long (length: {0}, max: {1})", length, maxLength);

            final long availableBytes = buffer.readableBytes();
            Check.argCondition(length > availableBytes, "Value is too long (length: {0}, available: {1})", length, availableBytes);
            final T value = parent.read(buffer);
            Check.argCondition(buffer.readableBytes() != availableBytes - length, "Value is too short (length: {0}, available: {1})", length, availableBytes);

            return value;
        }
    }

    static final class LazyType<T> implements Type<T> {
        private final Supplier<Type<T>> supplier;
        private @Nullable Type<T> type;

        public LazyType(Supplier<Type<T>> supplier) {
            this.supplier = Objects.requireNonNull(supplier, "supplier");
        }

        @Override
        public void write(NetworkBuffer buffer, T value) {
            type().write(buffer, value);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            return type().read(buffer);
        }

        private Type<T> type() {
            final Type<T> type = this.type;
            if (type == null) {
                return this.type = Objects.requireNonNull(supplier.get(), "type");
            }
            return type;
        }

        @Override
        public long sizeOf(T value, @Nullable Registries registries) {
            return type().sizeOf(value, registries);
        }
    }

    static final class RecursiveType<T> implements Type<T> {
        private final Type<T> delegate;

        public RecursiveType(UnaryOperator<Type<T>> supplier) {
            Objects.requireNonNull(supplier, "supplier");
            this.delegate = Objects.requireNonNull(supplier.apply(this), "delegate");
        }

        @Override
        public void write(NetworkBuffer buffer, T value) {
            delegate.write(buffer, value);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            return delegate.read(buffer);
        }

        public Type<T> delegate() {
            return this.delegate;
        }

        @Override
        public long sizeOf(T value, @Nullable Registries registries) {
            return delegate.sizeOf(value, registries);
        }
    }

    record TypedNbtType<T>(Codec<T> nbtType) implements Type<T> {
        public TypedNbtType {
            Objects.requireNonNull(nbtType, "nbtType");
        }

        @Override
        public void write(NetworkBuffer buffer, T value) {
            final Registries registries = buffer.registries();
            Check.stateCondition(registries == null, "Buffer does not have registries");
            final Result<BinaryTag> result = nbtType.encode(new RegistryTranscoder<>(Transcoder.NBT, registries), value);
            switch (result) {
                case Result.Ok(BinaryTag tag) -> buffer.write(NBT, tag);
                case Result.Error(String message) -> throw new IllegalArgumentException("Invalid NBT tag: " + message);
            }
        }

        @Override
        public T read(NetworkBuffer buffer) {
            final Registries registries = buffer.registries();
            Check.stateCondition(registries == null, "Buffer does not have registries");
            final Result<T> result = nbtType.decode(new RegistryTranscoder<>(Transcoder.NBT, registries), buffer.read(NBT));
            return switch (result) {
                case Result.Ok(T value) -> value;
                case Result.Error(String message) -> throw new IllegalArgumentException("Invalid NBT tag: " + message);
            };
        }
    }

    record EitherType<L, R>(
            Type<L> left,
            Type<R> right
    ) implements Type<Either<L, R>> {
        public EitherType {
            Objects.requireNonNull(left, "left");
            Objects.requireNonNull(right, "right");
        }

        @Override
        public void write(NetworkBuffer buffer, Either<L, R> value) {
            switch (value) {
                case Either.Left(L leftValue) -> {
                    BOOLEAN.writeBoolean(buffer, true);
                    left.write(buffer, leftValue);
                }
                case Either.Right(R rightValue) -> {
                    BOOLEAN.writeBoolean(buffer, false);
                    right.write(buffer, rightValue);
                }
            }
        }

        @Override
        public Either<L, R> read(NetworkBuffer buffer) {
            if (BOOLEAN.readBoolean(buffer))
                return Either.left(left.read(buffer));
            return Either.right(right.read(buffer));
        }

        @Override
        public long sizeOf(Either<L, R> value, @Nullable Registries registries) {
            return switch (value) {
                case Either.Left(L leftValue) ->
                        BOOLEAN.sizeOf(true, registries) + left().sizeOf(leftValue, registries);
                case Either.Right(R rightValue) ->
                        BOOLEAN.sizeOf(false, registries) + right().sizeOf(rightValue, registries);
            };
        }
    }

    record TransformType<T, S>(Type<T> parent,
                               Function<? super T, ? extends S> to,
                               Function<? super S, ? extends T> from) implements TransformingType<T, S> {
        public TransformType {
            Objects.requireNonNull(parent, "parent");
            Objects.requireNonNull(to, "to");
            Objects.requireNonNull(from, "from");
        }

        @Override
        public void write(NetworkBuffer buffer, S value) {
            parent.write(buffer, from.apply(value));
        }

        @Override
        public S read(NetworkBuffer buffer) {
            return to.apply(parent.read(buffer));
        }

        @Override
        public long sizeOf(S value) {
            return parent.sizeOf(from.apply(value));
        }
    }

    record MapType<K, V>(Type<K> parent, Type<V> valueType,
                         int maxSize) implements Type<Map<K, V>> {
        public MapType {
            Objects.requireNonNull(parent, "parent");
            Objects.requireNonNull(valueType, "valueType");
            Check.argCondition(maxSize < 0, "Max size is negative found {0}", maxSize);
        }

        @Override
        public void write(NetworkBuffer buffer, Map<K, V> map) {
            VAR_INT.writeInt(buffer, map.size());
            for (Map.Entry<K, V> entry : map.entrySet()) {
                parent.write(buffer, entry.getKey());
                valueType.write(buffer, entry.getValue());
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<K, V> read(NetworkBuffer buffer) {
            final int size = VAR_INT.readInt(buffer);
            Check.argCondition(size > maxSize, "Map size ({0}) is higher than the maximum allowed size ({1})", size, maxSize);
            K[] keys = (K[]) new Object[size];
            V[] values = (V[]) new Object[size];
            for (int i = 0; i < size; i++) {
                keys[i] = parent.read(buffer);
                values[i] = valueType.read(buffer);
            }
            return ArrayUtils.toMap(keys, values, size);
        }
    }

    record ListType<T>(Type<T> parent, int maxSize) implements Type<List<T>> {
        public ListType {
            Objects.requireNonNull(parent, "parent");
            Check.argCondition(maxSize < 0, "Max size is negative found {0}", maxSize);
        }

        @Override
        public void write(NetworkBuffer buffer, @Nullable List<T> values) {
            if (values == null) {
                BYTE.writeByte(buffer, (byte) 0);
                return;
            }
            VAR_INT.writeInt(buffer, values.size());
            for (T value : values) parent.write(buffer, value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<T> read(NetworkBuffer buffer) {
            final int size = VAR_INT.readInt(buffer);
            Check.argCondition(size > maxSize, "Collection size ({0}) is higher than the maximum allowed size ({1})", size, maxSize);
            T[] values = (T[]) new Object[size];
            for (int i = 0; i < size; i++) values[i] = parent.read(buffer);
            return List.of(values);
        }
    }

    record SetType<T>(Type<T> parent, int maxSize) implements Type<Set<T>> {
        public SetType {
            Objects.requireNonNull(parent, "parent");
            Check.argCondition(maxSize < 0, "Max size is negative found {0}", maxSize);
        }

        @Override
        public void write(NetworkBuffer buffer, @Nullable Set<T> values) {
            if (values == null) {
                BYTE.writeByte(buffer, (byte) 0);
                return;
            }
            VAR_INT.writeInt(buffer, values.size());
            for (T value : values) parent.write(buffer, value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<T> read(NetworkBuffer buffer) {
            final int size = VAR_INT.readInt(buffer);
            Check.argCondition(size > maxSize, "Collection size ({0}) is higher than the maximum allowed size ({1})", size, maxSize);
            T[] values = (T[]) new Object[size];
            for (int i = 0; i < size; i++) values[i] = parent.read(buffer);
            return Set.of(values);
        }
    }

    record UnionType<T, K>(
            Type<K> keyType, Function<? super T, ? extends K> keyFunc,
            Function<K, NetworkBuffer.@Nullable Type<? extends T>> serializers
    ) implements Type<T> {
        public UnionType {
            Objects.requireNonNull(keyType, "keyType");
            Objects.requireNonNull(keyFunc, "keyFunc");
            Objects.requireNonNull(serializers, "serializers");
        }

        @SuppressWarnings("unchecked") // Much nicer than using the correct wildcard type for returns, pretty much ensuring T has subtypes already.
        @Override
        public void write(NetworkBuffer buffer, T value) {
            final K key = keyFunc.apply(value);
            buffer.write(keyType, key);
            var serializer = serializers.apply(key);
            if (serializer == null)
                throw new UnsupportedOperationException("Unrecognized type: " + key);
            ((Type<T>) serializer).write(buffer, value);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            final K key = buffer.read(keyType);
            var serializer = serializers.apply(key);
            if (serializer == null)
                throw new UnsupportedOperationException("Unrecognized type: " + key);
            return serializer.read(buffer);
        }
    }

    /**
     * Used to write Java's UTF format, used primarily for {@link NetworkBuffer.IOView#writeUTF(String)}
     * This is not a pretty gross implementation cause it closely follows {@link java.io.DataOutputStream}
     * which optimizes for ascii for both read and write. This is quite expensive to write regardless as it requires
     * a few iterations to write.
     */
    record StringIOUTFType() implements Type<String> {
        @SuppressWarnings("deprecation") // Follows java.io.DataOutputStream#writeUTF(DataOutput, String) for JDK 25, not public sadly.
        @Override
        public void write(NetworkBuffer buffer, String value) {
            final int strlen = value.length();
            int utflen = strlen; // optimized for ASCII
            int copyableBytes = 0;

            for (int i = 0; i < strlen; i++) {
                int c = value.charAt(i);
                if (c >= 0x80 || c == 0)
                    utflen += (c >= 0x800) ? 2 : 1;
                if (strlen == utflen)
                    copyableBytes++; // We have no access to JLA for this.
            }

            if (utflen > 65535 || /* overflow */ utflen < strlen)
                throw new RuntimeException("UTF-8 string too long");

            UNSIGNED_SHORT.writeInt(buffer, utflen);
            buffer.ensureWritable(utflen); // throw early if possible
            var impl = buffer.direct();
            long offset = buffer.writeIndex();
            if (copyableBytes > 0) { // write if we have any copyableBytes
                byte[] ascii = new byte[copyableBytes];
                value.getBytes(0, copyableBytes, ascii, 0);
                impl.putBytes(offset, ascii);
                offset += copyableBytes;
            }

            for (int i = copyableBytes; i < strlen; i++) { // Excerpt from ModifiedUtf#putChar
                int c = value.charAt(i);
                if (c != 0 && c < 0x80) {
                    impl.putByte(offset++, (byte) c);
                } else if (c >= 0x800) {
                    impl.putByte(offset++, (byte) (0xE0 | c >> 12 & 0x0F));
                    impl.putByte(offset++, (byte) (0x80 | c >> 6  & 0x3F));
                    impl.putByte(offset++, (byte) (0x80 | c       & 0x3F));
                } else {
                    impl.putByte(offset++, (byte) (0xC0 | c >> 6 & 0x1F));
                    impl.putByte(offset++, (byte) (0x80 | c      & 0x3F));
                }
            }
            buffer.writeIndex(offset);
        }

        @Override
        public String read(NetworkBuffer buffer) {
            var ioView = buffer.ioView();
            try { // DataInputStream only has readUTF sadly.
                return DataInputStream.readUTF(ioView);
            } catch (IOException e) {
                throw new IllegalStateException("failed to read string", e);
            }
        }
    }
}
