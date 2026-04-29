package net.minestom.server.network;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.json.JsonUtil;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minestom.server.network.NetworkBuffer.*;
import static net.minestom.server.network.NetworkBufferImpl.impl;

/**
 * All built-in {@link NetworkBuffer.Type} implementations.
 *
 * <p>The only change from the original is the removal of the
 * {@code NetworkBufferUnsafe} import and the elimination of all
 * {@code sun.misc.Unsafe} calls — every read/write now goes through
 * {@link NetworkBufferImpl}'s Netty-backed accessors
 * ({@code _getByte}, {@code _putByte}, etc.).
 */
interface NetworkBufferTypeImpl<T> extends NetworkBuffer.Type<T> {

    int SEGMENT_BITS = 0x7F;
    int CONTINUE_BIT = 0x80;

    record UnitType() implements NetworkBufferTypeImpl<Unit> {
        @Override public void write(NetworkBuffer buffer, Unit value) {}
        @Override public Unit read(NetworkBuffer buffer) { return Unit.INSTANCE; }
    }

    record BooleanType() implements NetworkBufferTypeImpl<Boolean> {
        @Override
        public void write(NetworkBuffer buffer, Boolean value) {
            buffer.ensureWritable(1);
            impl(buffer)._putByte(buffer.writeIndex(), value ? (byte) 1 : (byte) 0);
            buffer.advanceWrite(1);
        }
        @Override
        public Boolean read(NetworkBuffer buffer) {
            final byte v = impl(buffer)._getByte(buffer.readIndex());
            buffer.advanceRead(1);
            return v == 1;
        }
    }

    record ByteType() implements NetworkBufferTypeImpl<Byte> {
        @Override
        public void write(NetworkBuffer buffer, Byte value) {
            buffer.ensureWritable(1);
            impl(buffer)._putByte(buffer.writeIndex(), value);
            buffer.advanceWrite(1);
        }
        @Override
        public Byte read(NetworkBuffer buffer) {
            final byte v = impl(buffer)._getByte(buffer.readIndex());
            buffer.advanceRead(1);
            return v;
        }
    }

    record UnsignedByteType() implements NetworkBufferTypeImpl<Short> {
        @Override
        public void write(NetworkBuffer buffer, Short value) {
            buffer.ensureWritable(1);
            impl(buffer)._putByte(buffer.writeIndex(), (byte) (value & 0xFF));
            buffer.advanceWrite(1);
        }
        @Override
        public Short read(NetworkBuffer buffer) {
            final byte v = impl(buffer)._getByte(buffer.readIndex());
            buffer.advanceRead(1);
            return (short) (v & 0xFF);
        }
    }

    record ShortType() implements NetworkBufferTypeImpl<Short> {
        @Override
        public void write(NetworkBuffer buffer, Short value) {
            buffer.ensureWritable(2);
            impl(buffer)._putShort(buffer.writeIndex(), value);
            buffer.advanceWrite(2);
        }
        @Override
        public Short read(NetworkBuffer buffer) {
            final short v = impl(buffer)._getShort(buffer.readIndex());
            buffer.advanceRead(2);
            return v;
        }
    }

    record UnsignedShortType() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer value) {
            buffer.ensureWritable(2);
            impl(buffer)._putShort(buffer.writeIndex(), (short) (value & 0xFFFF));
            buffer.advanceWrite(2);
        }
        @Override
        public Integer read(NetworkBuffer buffer) {
            final short v = impl(buffer)._getShort(buffer.readIndex());
            buffer.advanceRead(2);
            return v & 0xFFFF;
        }
    }

    record IntType() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer value) {
            buffer.ensureWritable(4);
            impl(buffer)._putInt(buffer.writeIndex(), value);
            buffer.advanceWrite(4);
        }
        @Override
        public Integer read(NetworkBuffer buffer) {
            final int v = impl(buffer)._getInt(buffer.readIndex());
            buffer.advanceRead(4);
            return v;
        }
    }

    record UnsignedIntType() implements NetworkBufferTypeImpl<Long> {
        @Override
        public void write(NetworkBuffer buffer, Long value) {
            buffer.ensureWritable(4);
            impl(buffer)._putInt(buffer.writeIndex(), (int) (value & 0xFFFFFFFFL));
            buffer.advanceWrite(4);
        }
        @Override
        public Long read(NetworkBuffer buffer) {
            final int v = impl(buffer)._getInt(buffer.readIndex());
            buffer.advanceRead(4);
            return v & 0xFFFFFFFFL;
        }
    }

    record LongType() implements NetworkBufferTypeImpl<Long> {
        @Override
        public void write(NetworkBuffer buffer, Long value) {
            buffer.ensureWritable(8);
            impl(buffer)._putLong(buffer.writeIndex(), value);
            buffer.advanceWrite(8);
        }
        @Override
        public Long read(NetworkBuffer buffer) {
            final long v = impl(buffer)._getLong(buffer.readIndex());
            buffer.advanceRead(8);
            return v;
        }
    }

    record FloatType() implements NetworkBufferTypeImpl<Float> {
        @Override
        public void write(NetworkBuffer buffer, Float value) {
            buffer.ensureWritable(4);
            impl(buffer)._putFloat(buffer.writeIndex(), value);
            buffer.advanceWrite(4);
        }
        @Override
        public Float read(NetworkBuffer buffer) {
            final float v = impl(buffer)._getFloat(buffer.readIndex());
            buffer.advanceRead(4);
            return v;
        }
    }

    record DoubleType() implements NetworkBufferTypeImpl<Double> {
        @Override
        public void write(NetworkBuffer buffer, Double value) {
            buffer.ensureWritable(8);
            impl(buffer)._putDouble(buffer.writeIndex(), value);
            buffer.advanceWrite(8);
        }
        @Override
        public Double read(NetworkBuffer buffer) {
            final double v = impl(buffer)._getDouble(buffer.readIndex());
            buffer.advanceRead(8);
            return v;
        }
    }

    record VarIntType() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer boxed) {
            buffer.ensureWritable(5);
            long index = buffer.writeIndex();
            int value = boxed;
            final var nio = impl(buffer);
            while (true) {
                if ((value & ~SEGMENT_BITS) == 0) {
                    nio._putByte(index++, (byte) value);
                    buffer.advanceWrite(index - buffer.writeIndex());
                    return;
                }
                nio._putByte(index++, (byte) ((value & SEGMENT_BITS) | CONTINUE_BIT));
                value >>>= 7;
            }
        }
        @Override
        public Integer read(NetworkBuffer buffer) {
            long index = buffer.readIndex();
            int result = 0;
            for (int shift = 0; ; shift += 7) {
                byte b = impl(buffer)._getByte(index++);
                result |= (b & 0x7f) << shift;
                if (b >= 0) {
                    buffer.advanceRead(index - buffer.readIndex());
                    return result;
                }
            }
        }
    }

    record OptionalVarIntType() implements NetworkBufferTypeImpl<@Nullable Integer> {
        @Override
        public void write(NetworkBuffer buffer, @Nullable Integer value) {
            buffer.write(VAR_INT, value == null ? 0 : value + 1);
        }
        @Override
        public @Nullable Integer read(NetworkBuffer buffer) {
            final int v = buffer.read(VAR_INT);
            return v == 0 ? null : v - 1;
        }
    }

    record VarInt3Type() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer boxed) {
            final int value = boxed;
            // Value must be between 0 and 2^21
            Check.argCondition(value < 0 || value >= (1 << 21), "VarInt3 out of bounds: {0}", value);
            buffer.ensureWritable(3);
            final long startIndex = buffer.writeIndex();
            var impl = impl(buffer);
            impl._putByte(startIndex, (byte) (value & 0x7F | 0x80));
            impl._putByte(startIndex + 1, (byte) ((value >>> 7) & 0x7F | 0x80));
            impl._putByte(startIndex + 2, (byte) (value >>> 14));
            buffer.advanceWrite(3);
        }

        @Override
        public Integer read(NetworkBuffer buffer) {
            // Ensure that the buffer can read other var-int sizes
            // The optimization is mostly relevant for writing
            return buffer.read(VAR_INT);
        }
    }

    record VarLongType() implements NetworkBufferTypeImpl<Long> {
        @Override
        public void write(NetworkBuffer buffer, Long value) {
            buffer.ensureWritable(10);
            int size = 0;
            while (true) {
                if ((value & ~((long) SEGMENT_BITS)) == 0) {
                    impl(buffer)._putByte(buffer.writeIndex() + size, (byte) value.intValue());
                    buffer.advanceWrite(size + 1);
                    return;
                }
                impl(buffer)._putByte(buffer.writeIndex() + size,
                        (byte) (value & SEGMENT_BITS | CONTINUE_BIT));
                size++;
                // note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
                value >>>= 7;
            }
        }

        @Override
        public Long read(NetworkBuffer buffer) {
            int length = 0;
            long value = 0;
            int position = 0;
            byte current;
            while (true) {
                current = impl(buffer)._getByte(buffer.readIndex() + length);
                length++;
                value |= (long) (current & SEGMENT_BITS) << position;
                if ((current & CONTINUE_BIT) == 0) break;
                position += 7;
                if (position >= 64) throw new RuntimeException("VarLong is too big");
            }
            buffer.advanceRead(length);
            return value;
        }
    }

    record RawBytesType(int length) implements NetworkBufferTypeImpl<byte[]> {
        @Override
        public void write(NetworkBuffer buffer, byte[] value) {
            if (length != -1 && value.length != length)
                throw new IllegalArgumentException("Invalid length: " + value.length + " != " + length);
            if (value.length == 0) return;
            buffer.ensureWritable(value.length);
            impl(buffer)._putBytes(buffer.writeIndex(), value);
            buffer.advanceWrite(value.length);
        }
        @Override
        public byte[] read(NetworkBuffer buffer) {
            long len = buffer.readableBytes();
            if (this.length != -1) len = Math.min(len, this.length);
            if (len == 0) return new byte[0];
            final int arrayLen = Math.toIntExact(len);
            final byte[] bytes = new byte[arrayLen];
            impl(buffer)._getBytes(buffer.readIndex(), bytes);
            buffer.advanceRead(arrayLen);
            return bytes;
        }
    }

    record StringType() implements NetworkBufferTypeImpl<String> {
        @Override
        public void write(NetworkBuffer buffer, String value) {
            buffer.write(BYTE_ARRAY, value.getBytes(StandardCharsets.UTF_8));
        }
        @Override
        public String read(NetworkBuffer buffer) {
            return new String(buffer.read(BYTE_ARRAY), StandardCharsets.UTF_8);
        }
    }

    record StringTerminatedType() implements NetworkBufferTypeImpl<String> {
        @Override
        public void write(NetworkBuffer buffer, String value) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            final byte[] terminated = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, terminated, 0, bytes.length);
            buffer.write(RAW_BYTES, terminated);
        }
        @Override
        public String read(NetworkBuffer buffer) {
            final ByteArrayList bytes = new ByteArrayList();
            byte b;
            while ((b = buffer.read(BYTE)) != 0) bytes.add(b);
            return new String(bytes.elements(), StandardCharsets.UTF_8);
        }
    }

    record NbtType() implements NetworkBufferTypeImpl<BinaryTag> {
        @Override
        public void write(NetworkBuffer buffer, BinaryTag value) {
            try {
                impl(buffer).nbtWriter().writeNameless(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public BinaryTag read(NetworkBuffer buffer) {
            try {
                return impl(buffer).nbtReader().readNameless();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    record BlockPositionType() implements NetworkBufferTypeImpl<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            final long longPos =
                    (((long) value.blockX() & 0x3FFFFFF) << 38) |
                            (((long) value.blockZ() & 0x3FFFFFF) << 12) |
                            ((long) value.blockY() & 0xFFF);
            buffer.write(LONG, longPos);
        }
        @Override
        public Point read(NetworkBuffer buffer) {
            final long v = buffer.read(LONG);
            return new Vec((int) (v >> 38), (int) (v << 52 >> 52), (int) (v << 26 >> 38));
        }
    }

    record JsonComponentType() implements NetworkBufferTypeImpl<Component> {
        @Override
        public void write(NetworkBuffer buffer, Component value) {
            final Transcoder<JsonElement> coder = buffer.registries() != null
                    ? new RegistryTranscoder<>(Transcoder.JSON, buffer.registries())
                    : Transcoder.JSON;
            buffer.write(STRING, JsonUtil.toJson(Codec.COMPONENT.encode(coder, value).orElseThrow()));
        }
        @Override
        public Component read(NetworkBuffer buffer) {
            final Transcoder<JsonElement> coder = buffer.registries() != null
                    ? new RegistryTranscoder<>(Transcoder.JSON, buffer.registries())
                    : Transcoder.JSON;
            return Codec.COMPONENT.decode(coder, JsonUtil.fromJson(buffer.read(STRING))).orElseThrow();
        }
    }

    record UUIDType() implements NetworkBufferTypeImpl<java.util.UUID> {
        @Override
        public void write(NetworkBuffer buffer, java.util.UUID value) {
            buffer.write(LONG, value.getMostSignificantBits());
            buffer.write(LONG, value.getLeastSignificantBits());
        }
        @Override
        public java.util.UUID read(NetworkBuffer buffer) {
            return new java.util.UUID(buffer.read(LONG), buffer.read(LONG));
        }
    }

    record PosType() implements NetworkBufferTypeImpl<Pos> {
        @Override
        public void write(NetworkBuffer buffer, Pos value) {
            buffer.write(DOUBLE, value.x());
            buffer.write(DOUBLE, value.y());
            buffer.write(DOUBLE, value.z());
            buffer.write(FLOAT,  value.yaw());
            buffer.write(FLOAT,  value.pitch());
        }
        @Override
        public Pos read(NetworkBuffer buffer) {
            return new Pos(buffer.read(DOUBLE), buffer.read(DOUBLE), buffer.read(DOUBLE),
                    buffer.read(FLOAT),  buffer.read(FLOAT));
        }
    }

    record ByteArrayType() implements NetworkBufferTypeImpl<byte[]> {
        @Override
        public void write(NetworkBuffer buffer, byte[] value) {
            buffer.write(VAR_INT, value.length);
            buffer.write(RAW_BYTES, value);
        }
        @Override
        public byte[] read(NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
            if (length == 0) return new byte[0];
            final long remaining = buffer.readableBytes();
            Check.argCondition(length > remaining,
                    "String is too long (length: {0}, readable: {1})", length, remaining);
            return buffer.read(FixedRawBytes(length));
        }
    }

    record LongArrayType() implements NetworkBufferTypeImpl<long[]> {
        @Override
        public void write(NetworkBuffer buffer, long[] value) {
            buffer.write(VAR_INT, value.length);
            for (long l : value) buffer.write(LONG, l);
        }
        @Override
        public long[] read(NetworkBuffer buffer) {
            final long[] longs = new long[buffer.read(VAR_INT)];
            for (int i = 0; i < longs.length; i++) longs[i] = buffer.read(LONG);
            return longs;
        }
    }

    record VarIntArrayType() implements NetworkBufferTypeImpl<int[]> {
        @Override
        public void write(NetworkBuffer buffer, int[] value) {
            buffer.write(VAR_INT, value.length);
            for (int i : value) buffer.write(VAR_INT, i);
        }
        @Override
        public int[] read(NetworkBuffer buffer) {
            final int[] ints = new int[buffer.read(VAR_INT)];
            for (int i = 0; i < ints.length; i++) ints[i] = buffer.read(VAR_INT);
            return ints;
        }
    }

    record VarLongArrayType() implements NetworkBufferTypeImpl<long[]> {
        @Override
        public void write(NetworkBuffer buffer, long[] value) {
            buffer.write(VAR_INT, value.length);
            for (long l : value) buffer.write(VAR_LONG, l);
        }
        @Override
        public long[] read(NetworkBuffer buffer) {
            final long[] longs = new long[buffer.read(VAR_INT)];
            for (int i = 0; i < longs.length; i++) longs[i] = buffer.read(VAR_LONG);
            return longs;
        }
    }

    record Vector3Type() implements NetworkBufferTypeImpl<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            buffer.write(FLOAT, (float) value.x());
            buffer.write(FLOAT, (float) value.y());
            buffer.write(FLOAT, (float) value.z());
        }
        @Override
        public Point read(NetworkBuffer buffer) {
            return new Vec(buffer.read(FLOAT), buffer.read(FLOAT), buffer.read(FLOAT));
        }
    }

    record Vector3DType() implements NetworkBufferTypeImpl<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            buffer.write(DOUBLE, value.x());
            buffer.write(DOUBLE, value.y());
            buffer.write(DOUBLE, value.z());
        }
        @Override
        public Point read(NetworkBuffer buffer) {
            return new Vec(buffer.read(DOUBLE), buffer.read(DOUBLE), buffer.read(DOUBLE));
        }
    }

    record Vector3IType() implements NetworkBufferTypeImpl<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            buffer.write(VAR_INT, (int) value.x());
            buffer.write(VAR_INT, (int) value.y());
            buffer.write(VAR_INT, (int) value.z());
        }
        @Override
        public Point read(NetworkBuffer buffer) {
            return new Vec(buffer.read(VAR_INT), buffer.read(VAR_INT), buffer.read(VAR_INT));
        }
    }

    record Vector3BType() implements NetworkBufferTypeImpl<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            buffer.write(BYTE, (byte) value.x());
            buffer.write(BYTE, (byte) value.y());
            buffer.write(BYTE, (byte) value.z());
        }
        @Override
        public Point read(NetworkBuffer buffer) {
            return new Vec(buffer.read(BYTE), buffer.read(BYTE), buffer.read(BYTE));
        }
    }

    record LpVector3Type() implements NetworkBufferTypeImpl<Vec> {
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
                buffer.write(BYTE, (byte) 0);
            } else {
                long i = MathUtils.ceilLong(max);
                boolean hasContinuation = (i & SCALE_BITS_MASK) != i;
                long flags = hasContinuation ? i & SCALE_BITS_MASK | CONTINUATION_FLAG : i;
                long px = pack(x / i) << X_OFFSET;
                long py = pack(y / i) << Y_OFFSET;
                long pz = pack(z / i) << Z_OFFSET;
                long packed = flags | px | py | pz;
                buffer.write(BYTE, (byte) packed);
                buffer.write(BYTE, (byte) (packed >> 8));
                buffer.write(INT, (int) (packed >> 16));
                if (hasContinuation) buffer.write(VAR_INT, (int) (i >> 2));
            }
        }

        @Override
        public Vec read(NetworkBuffer buffer) {
            int flags = buffer.read(UNSIGNED_BYTE);
            if (flags == 0) {
                return Vec.ZERO;
            } else {
                int p2 = buffer.read(UNSIGNED_BYTE);
                long p3 = buffer.read(UNSIGNED_INT);
                long value = p3 << 16 | p2 << 8 | flags;
                long scale = flags & SCALE_BITS_MASK;
                if ((flags & CONTINUATION_FLAG) == CONTINUATION_FLAG)
                    scale |= (buffer.read(VAR_INT) & 0xFFFFFFFFL) << 2;
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

    record QuaternionType() implements NetworkBufferTypeImpl<float[]> {
        @Override
        public void write(NetworkBuffer buffer, float[] value) {
            buffer.write(FLOAT, value[0]);
            buffer.write(FLOAT, value[1]);
            buffer.write(FLOAT, value[2]);
            buffer.write(FLOAT, value[3]);
        }
        @Override
        public float[] read(NetworkBuffer buffer) {
            final float x = buffer.read(FLOAT);
            final float y = buffer.read(FLOAT);
            final float z = buffer.read(FLOAT);
            final float w = buffer.read(FLOAT);
            return new float[]{x, y, z, w};
        }
    }

    record EnumSetType<E extends Enum<E>>(Class<E> enumType,
                                          E[] values) implements NetworkBufferTypeImpl<EnumSet<E>> {
        @Override
        public void write(NetworkBuffer buffer, EnumSet<E> value) {
            final BitSet bs = new BitSet(values.length);
            for (int i = 0; i < values.length; i++) bs.set(i, value.contains(values[i]));
            buffer.write(RAW_BYTES, bs.toByteArray());
        }
        @Override
        public EnumSet<E> read(NetworkBuffer buffer) {
            final BitSet bs = BitSet.valueOf(buffer.read(FixedRawBytes((values.length + 7) / 8)));
            final EnumSet<E> set = EnumSet.noneOf(enumType);
            for (int i = 0; i < values.length; i++) if (bs.get(i)) set.add(values[i]);
            return set;
        }
    }

    record FixedBitSetType(int length) implements NetworkBufferTypeImpl<BitSet> {
        @Override
        public void write(NetworkBuffer buffer, BitSet value) {
            if (value.length() > length)
                throw new IllegalArgumentException("BitSet larger than expected (" + value.length() + ">" + length + ")");
            buffer.write(RAW_BYTES, value.toByteArray());
        }
        @Override
        public BitSet read(NetworkBuffer buffer) {
            return BitSet.valueOf(buffer.read(FixedRawBytes((length + 7) / 8)));
        }
    }

    record OptionalType<T>(Type<T> parent) implements NetworkBufferTypeImpl<@Nullable T> {
        @Override
        public void write(NetworkBuffer buffer, T value) {
            buffer.write(BOOLEAN, value != null);
            if (value != null) buffer.write(parent, value);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            return buffer.read(BOOLEAN) ? buffer.read(parent) : null;
        }
    }

    record LengthPrefixedType<T>(Type<T> parent, int maxLength) implements NetworkBufferTypeImpl<T> {
        @Override
        public void write(NetworkBuffer buffer, T value) {
            // Write to another buffer and copy (kinda inefficient, but currently unused serverside so its ok for now)
            final byte[] componentData = NetworkBuffer.makeArray(b -> parent.write(b, value), buffer.registries());
            buffer.write(NetworkBuffer.BYTE_ARRAY, componentData);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
            Check.argCondition(length > maxLength,
                    "Value is too long (length: {0}, max: {1})", length, maxLength);
            final long available = buffer.readableBytes();
            Check.argCondition(length > available,
                    "Value is too long (length: {0}, available: {1})", length, available);
            final T value = parent.read(buffer);
            Check.argCondition(buffer.readableBytes() != available - length,
                    "Value is too short (length: {0}, available: {1})", length, available);
            return value;
        }
    }

    final class LazyType<T> implements NetworkBufferTypeImpl<T> {
        private final Supplier<NetworkBuffer.Type<T>> supplier;
        private Type<T> type;
        public LazyType(Supplier<NetworkBuffer.Type<T>> supplier) { this.supplier = supplier; }
        @Override public void write(NetworkBuffer buffer, T value) {
            if (type == null) type = supplier.get();
            type.write(buffer, value);
        }
        @Override public T read(NetworkBuffer buffer) {
            if (type == null) type = supplier.get();
            return null;
        }
    }

    record TypedNbtType<T>(Codec<T> nbtType) implements NetworkBufferTypeImpl<T> {
        @Override
        public void write(NetworkBuffer buffer, T value) {
            final Registries registries = impl(buffer).registries;
            Check.stateCondition(registries == null, "Buffer does not have registries");
            final Result<BinaryTag> result = nbtType.encode(new RegistryTranscoder<>(Transcoder.NBT, registries), value);
            switch (result) {
                case Result.Ok(BinaryTag tag) -> buffer.write(NBT, tag);
                case Result.Error(String message) -> throw new IllegalArgumentException("Invalid NBT tag: " + message);
            }
        }
        @Override
        public T read(NetworkBuffer buffer) {
            final Registries reg = impl(buffer).registries;
            Check.stateCondition(reg == null, "Buffer does not have registries");
            return switch (nbtType.decode(new RegistryTranscoder<>(Transcoder.NBT, reg), buffer.read(NBT))) {
                case Result.Ok(T v)           -> v;
                case Result.Error(String msg) -> throw new IllegalArgumentException("Invalid NBT tag: " + msg);
            };
        }
    }

    record EitherType<L, R>(
            NetworkBuffer.Type<L> left,
            NetworkBuffer.Type<R> right
    ) implements NetworkBuffer.Type<Either<L, R>> {
        @Override
        public void write(NetworkBuffer buffer, Either<L, R> value) {
            switch (value) {
                case Either.Left(L leftValue) -> {
                    buffer.write(BOOLEAN, true);
                    buffer.write(left, leftValue);
                }
                case Either.Right(R rightValue) -> {
                    buffer.write(BOOLEAN, false);
                    buffer.write(right, rightValue);
                }
            }
        }
        @Override
        public Either<L, R> read(NetworkBuffer buffer) {
            return buffer.read(BOOLEAN)
                    ? Either.left(buffer.read(left))
                    : Either.right(buffer.read(right));
        }
    }

    record TransformType<T, S>(Type<T> parent,
                               Function<T, S> to,
                               Function<S, T> from) implements NetworkBufferTypeImpl<S> {
        @Override
        public void write(NetworkBuffer buffer, S value) {
            parent.write(buffer, from.apply(value));
        }

        @Override
        public S read(NetworkBuffer buffer) {
            return to.apply(parent.read(buffer));
        }
    }

    record MapType<K, V>(Type<K> parent, NetworkBuffer.Type<V> valueType,
                         int maxSize) implements NetworkBufferTypeImpl<Map<K, V>> {
        @Override
        public void write(NetworkBuffer buffer, Map<K, V> map) {
            buffer.write(VAR_INT, map.size());
            for (Map.Entry<K, V> entry : map.entrySet()) {
                buffer.write(parent, entry.getKey());
                buffer.write(valueType, entry.getValue());
            }
        }
        @SuppressWarnings("unchecked")
        @Override
        public Map<K, V> read(NetworkBuffer buffer) {
            final int size = buffer.read(VAR_INT);
            Check.argCondition(size > maxSize, "Map size ({0}) is higher than the maximum allowed size ({1})", size, maxSize);
            K[] keys = (K[]) new Object[size];
            V[] values = (V[]) new Object[size];
            for (int i = 0; i < size; i++) {
                keys[i] = buffer.read(parent);
                values[i] = buffer.read(valueType);
            }
            return Map.copyOf(new Object2ObjectArrayMap<>(keys, values, size));
        }
    }

    record ListType<T>(Type<T> parent, int maxSize) implements NetworkBufferTypeImpl<List<T>> {
        @Override
        public void write(NetworkBuffer buffer, List<T> values) {
            if (values == null) { buffer.write(BYTE, (byte) 0); return; }
            buffer.write(VAR_INT, values.size());
            for (T v : values) buffer.write(parent, v);
        }
        @SuppressWarnings("unchecked")
        @Override
        public List<T> read(NetworkBuffer buffer) {
            final int size = buffer.read(VAR_INT);
            Check.argCondition(size > maxSize, "Collection size ({0}) > max ({1})", size, maxSize);
            T[] values = (T[]) new Object[size];
            for (int i = 0; i < size; i++) values[i] = buffer.read(parent);
            return List.of(values);
        }
    }

    record SetType<T>(Type<T> parent, int maxSize) implements NetworkBufferTypeImpl<Set<T>> {
        @Override
        public void write(NetworkBuffer buffer, Set<T> values) {
            if (values == null) { buffer.write(BYTE, (byte) 0); return; }
            buffer.write(VAR_INT, values.size());
            for (T v : values) buffer.write(parent, v);
        }
        @SuppressWarnings("unchecked")
        @Override
        public Set<T> read(NetworkBuffer buffer) {
            final int size = buffer.read(VAR_INT);
            Check.argCondition(size > maxSize, "Collection size ({0}) > max ({1})", size, maxSize);
            T[] values = (T[]) new Object[size];
            for (int i = 0; i < size; i++) values[i] = buffer.read(parent);
            return Set.of(values);
        }
    }

    record UnionType<T, K, TR extends T>(
            Type<K> keyType,
            Function<T, ? extends K> keyFunc,
            Function<K, NetworkBuffer.Type<TR>> serializers
    ) implements NetworkBufferTypeImpl<T> {
        @SuppressWarnings("unchecked")
        @Override
        public void write(NetworkBuffer buffer, T value) {
            final K key = keyFunc.apply(value);
            buffer.write(keyType, key);
            final var ser = serializers.apply(key);
            if (ser == null) throw new UnsupportedOperationException("Unrecognized type: " + key);
            ser.write(buffer, (TR) value);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            final K key = buffer.read(keyType);
            final var ser = serializers.apply(key);
            if (ser == null) throw new UnsupportedOperationException("Unrecognized type: " + key);
            return ser.read(buffer);
        }
    }

    record IOUTF8StringType() implements NetworkBufferTypeImpl<String> {
        @Override
        public void write(NetworkBuffer buffer, String value) {
            final int strlen = value.length();
            int utflen = strlen; // optimized for ASCII

            for (int i = 0; i < strlen; i++) {
                int c = value.charAt(i);
                if (c >= 0x80 || c == 0) utflen += (c >= 0x800) ? 2 : 1;
            }

            if (utflen > 65535 || /* overflow */ utflen < strlen)
                throw new RuntimeException("UTF-8 string too long");
            buffer.write(SHORT, (short) utflen);
            buffer.ensureWritable(utflen);
            var impl = (NetworkBufferImpl) buffer;
            int i;
            for (i = 0; i < strlen; i++) { // optimized for initial run of ASCII
                int c = value.charAt(i);
                if (c >= 0x80 || c == 0) break;
                impl._putByte(buffer.writeIndex(), (byte) c);
                impl.advanceWrite(1);
            }
            for (; i < strlen; i++) {
                int c = value.charAt(i);
                if (c < 0x80 && c != 0) {
                    impl._putByte(buffer.writeIndex(), (byte) c);
                    impl.advanceWrite(1);
                } else if (c >= 0x800) {
                    impl._putByte(buffer.writeIndex(), (byte) (0xE0 | ((c >> 12) & 0x0F)));
                    impl._putByte(buffer.writeIndex() + 1, (byte) (0x80 | ((c >> 6) & 0x3F)));
                    impl._putByte(buffer.writeIndex() + 2, (byte) (0x80 | ((c >> 0) & 0x3F)));
                    impl.advanceWrite(3);
                } else {
                    impl._putByte(buffer.writeIndex(), (byte) (0xC0 | ((c >> 6) & 0x1F)));
                    impl._putByte(buffer.writeIndex() + 1, (byte) (0x80 | ((c >> 0) & 0x3F)));
                    impl.advanceWrite(2);
                }
            }
        }

        @Override
        public String read(NetworkBuffer buffer) {
            final int utflen = buffer.read(UNSIGNED_SHORT);
            if (buffer.readableBytes() < utflen)
                throw new IllegalArgumentException("Invalid String size.");
            final byte[] bytearr = buffer.read(FixedRawBytes(utflen));
            final char[] chararr = new char[utflen];
            int c, char2, char3, count = 0, chararr_count = 0;
            while (count < utflen) {
                c = bytearr[count] & 0xFF;
                if (c > 127) break;
                count++;
                chararr[chararr_count++] = (char) c;
            }
            while (count < utflen) {
                c = bytearr[count] & 0xFF;
                try {
                    switch (c >> 4) {
                        case 0, 1, 2, 3, 4, 5, 6, 7 -> { count++; chararr[chararr_count++] = (char) c; }
                        case 12, 13 -> {
                            count += 2;
                            if (count > utflen) throw new UTFDataFormatException("partial char at end");
                            char2 = bytearr[count - 1];
                            if ((char2 & 0xC0) != 0x80) throw new UTFDataFormatException("malformed @" + count);
                            chararr[chararr_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                        }
                        case 14 -> {
                            count += 3;
                            if (count > utflen) throw new UTFDataFormatException("partial char at end");
                            char2 = bytearr[count - 2];
                            char3 = bytearr[count - 1];
                            if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                                throw new UTFDataFormatException("malformed @" + (count - 1));
                            chararr[chararr_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | (char3 & 0x3F));
                        }
                        default -> throw new UTFDataFormatException("malformed @" + count);
                    }
                } catch (UTFDataFormatException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return new String(chararr, 0, chararr_count);
        }
    }

    static <T> long sizeOf(Type<T> type, T value, Registries registries) {
        final NetworkBuffer dummy = NetworkBufferImpl.dummy(registries);
        type.write(dummy, value);
        return dummy.writeIndex();
    }
}