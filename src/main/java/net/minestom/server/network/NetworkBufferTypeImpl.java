package net.minestom.server.network;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Unit;
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

import static net.minestom.server.network.NetworkBuffer.*;

final class NetworkBufferTypeImpl {
    static final int SEGMENT_BITS = 0x7F;
    static final int CONTINUE_BIT = 0x80;

    record UnitType() implements Type<Unit> {
        @Override
        public void write(NetworkBuffer buffer, Unit value) {
        }

        @Override
        public Unit read(NetworkBuffer buffer) {
            return Unit.INSTANCE;
        }
    }

    record BooleanType() implements Type<Boolean> {
        @Override
        public void write(NetworkBuffer buffer, Boolean value) {
            buffer.ensureWritable(1);
            buffer.direct().putByte(buffer.writeIndex(), value ? (byte) 1 : (byte) 0);
            buffer.advanceWrite(1);
        }

        @Override
        public Boolean read(NetworkBuffer buffer) {
            final byte value = buffer.direct().getByte(buffer.readIndex());
            buffer.advanceRead(1);
            return value == 1;
        }
    }

    record ByteType() implements Type<Byte> {
        @Override
        public void write(NetworkBuffer buffer, Byte value) {
            buffer.ensureWritable(1);
            buffer.direct().putByte(buffer.writeIndex(), value);
            buffer.advanceWrite(1);
        }

        @Override
        public Byte read(NetworkBuffer buffer) {
            final byte value = buffer.direct().getByte(buffer.readIndex());
            buffer.advanceRead(1);
            return value;
        }
    }

    record UnsignedByteType() implements Type<Short> {
        @Override
        public void write(NetworkBuffer buffer, Short value) {
            buffer.ensureWritable(1);
            buffer.direct().putByte(buffer.writeIndex(), (byte) (value & 0xFF));
            buffer.advanceWrite(1);
        }

        @Override
        public Short read(NetworkBuffer buffer) {
            final byte value = buffer.direct().getByte(buffer.readIndex());
            buffer.advanceRead(1);
            return (short) (value & 0xFF);
        }
    }

    record ShortType() implements Type<Short> {
        @Override
        public void write(NetworkBuffer buffer, Short value) {
            buffer.ensureWritable(2);
            buffer.direct().putShort(buffer.writeIndex(), value);
            buffer.advanceWrite(2);
        }

        @Override
        public Short read(NetworkBuffer buffer) {
            final short value = buffer.direct().getShort(buffer.readIndex());
            buffer.advanceRead(2);
            return value;
        }
    }

    record UnsignedShortType() implements Type<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer value) {
            buffer.ensureWritable(2);
            buffer.direct().putShort(buffer.writeIndex(), (short) (value & 0xFFFF));
            buffer.advanceWrite(2);
        }

        @Override
        public Integer read(NetworkBuffer buffer) {
            final short value = buffer.direct().getShort(buffer.readIndex());
            buffer.advanceRead(2);
            return value & 0xFFFF;
        }
    }

    record IntType() implements Type<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer value) {
            buffer.ensureWritable(4);
            buffer.direct().putInt(buffer.writeIndex(), value);
            buffer.advanceWrite(4);
        }

        @Override
        public Integer read(NetworkBuffer buffer) {
            final int value = buffer.direct().getInt(buffer.readIndex());
            buffer.advanceRead(4);
            return value;
        }
    }

    record UnsignedIntType() implements Type<Long> {
        @Override
        public void write(NetworkBuffer buffer, Long value) {
            buffer.ensureWritable(4);
            buffer.direct().putInt(buffer.writeIndex(), (int) (value & 0xFFFFFFFFL));
            buffer.advanceWrite(4);
        }

        @Override
        public Long read(NetworkBuffer buffer) {
            final int value = buffer.direct().getInt(buffer.readIndex());
            buffer.advanceRead(4);
            return value & 0xFFFFFFFFL;
        }
    }

    record LongType() implements Type<Long> {
        @Override
        public void write(NetworkBuffer buffer, Long value) {
            buffer.ensureWritable(8);
            buffer.direct().putLong(buffer.writeIndex(), value);
            buffer.advanceWrite(8);
        }

        @Override
        public Long read(NetworkBuffer buffer) {
            final long value = buffer.direct().getLong(buffer.readIndex());
            buffer.advanceRead(8);
            return value;
        }
    }

    record FloatType() implements Type<Float> {
        @Override
        public void write(NetworkBuffer buffer, Float value) {
            buffer.ensureWritable(4);
            buffer.direct().putFloat(buffer.writeIndex(), value);
            buffer.advanceWrite(4);
        }

        @Override
        public Float read(NetworkBuffer buffer) {
            final float value = buffer.direct().getFloat(buffer.readIndex());
            buffer.advanceRead(4);
            return value;
        }
    }

    record DoubleType() implements Type<Double> {
        @Override
        public void write(NetworkBuffer buffer, Double value) {
            buffer.ensureWritable(8);
            buffer.direct().putDouble(buffer.writeIndex(), value);
            buffer.advanceWrite(8);
        }

        @Override
        public Double read(NetworkBuffer buffer) {
            final double value = buffer.direct().getDouble(buffer.readIndex());
            buffer.advanceRead(8);
            return value;
        }
    }

    record VarIntType() implements Type<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer boxed) {
            buffer.ensureWritable(5);
            long index = buffer.writeIndex();
            int value = boxed;
            var nio = buffer.direct();
            while (true) {
                if ((value & ~SEGMENT_BITS) == 0) {
                    nio.putByte(index++, (byte) value);
                    buffer.advanceWrite(index - buffer.writeIndex());
                    return;
                }
                nio.putByte(index++, (byte) ((byte) (value & SEGMENT_BITS) | CONTINUE_BIT));
                // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
                value >>>= 7;
            }
        }

        @Override
        public Integer read(NetworkBuffer buffer) {
            long index = buffer.readIndex();
            // https://github.com/jvm-profiling-tools/async-profiler/blob/a38a375dc62b31a8109f3af97366a307abb0fe6f/src/converter/one/jfr/JfrReader.java#L393
            int result = 0;
            for (int shift = 0; ; shift += 7) {
                byte b = buffer.direct().getByte(index++);
                result |= (b & 0x7f) << shift;
                if (b >= 0) {
                    buffer.advanceRead(index - buffer.readIndex());
                    return result;
                }
            }
        }
    }

    record OptionalVarIntType() implements Type<@Nullable Integer> {
        @Override
        public void write(NetworkBuffer buffer, @Nullable Integer value) {
            buffer.write(VAR_INT, value == null ? 0 : value + 1);
        }

        @Override
        public @Nullable Integer read(NetworkBuffer buffer) {
            final int value = buffer.read(VAR_INT);
            return value == 0 ? null : value - 1;
        }
    }

    record VarInt3Type() implements Type<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer boxed) {
            final int value = boxed;
            // Value must be between 0 and 2^21
            Check.argCondition(value < 0 || value >= (1 << 21), "VarInt3 out of bounds: {0}", value);
            buffer.ensureWritable(3);
            final long startIndex = buffer.writeIndex();
            var impl = buffer.direct();
            impl.putByte(startIndex, (byte) (value & 0x7F | 0x80));
            impl.putByte(startIndex + 1, (byte) ((value >>> 7) & 0x7F | 0x80));
            impl.putByte(startIndex + 2, (byte) (value >>> 14));
            buffer.advanceWrite(3);
        }

        @Override
        public Integer read(NetworkBuffer buffer) {
            // Ensure that the buffer can read other var-int sizes
            // The optimization is mostly relevant for writing
            return buffer.read(VAR_INT);
        }
    }

    record VarLongType() implements Type<Long> {
        @Override
        public void write(NetworkBuffer buffer, Long value) {
            buffer.ensureWritable(10);
            int size = 0;
            while (true) {
                if ((value & ~((long) SEGMENT_BITS)) == 0) {
                    buffer.direct().putByte(buffer.writeIndex() + size, (byte) value.intValue());
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
        public Long read(NetworkBuffer buffer) {
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
                if (position >= 64) throw new RuntimeException("VarLong is too big");
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
            if (length == 0) return; // TODO, should we allow zero length when length is fixed?
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
    }

    record StringType() implements Type<String> {
        @Override
        public void write(NetworkBuffer buffer, String value) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            buffer.write(BYTE_ARRAY, bytes);
        }

        @Override
        public String read(NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
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
            while ((b = buffer.read(BYTE)) != 0) {
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
                buffer.write(BYTE, (byte) 0x00);
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
            buffer.write(LONG, longPos);
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            final long value = buffer.read(LONG);
            final int x = (int) (value >> 38);
            final int y = (int) (value << 52 >> 52);
            final int z = (int) (value << 26 >> 38);
            return new Vec(x, y, z);
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
            buffer.write(STRING, json);
        }

        @Override
        public Component read(NetworkBuffer buffer) {
            final Registries registries = buffer.registries();
            final Transcoder<JsonElement> coder = registries != null
                    ? new RegistryTranscoder<>(Transcoder.JSON, registries)
                    : Transcoder.JSON;
            final JsonElement json = JsonUtil.fromJson(buffer.read(STRING));
            return Codec.COMPONENT.decode(coder, json).orElseThrow();
        }
    }

    record UUIDType() implements Type<UUID> {
        @Override
        public void write(NetworkBuffer buffer, java.util.UUID value) {
            buffer.write(LONG, value.getMostSignificantBits());
            buffer.write(LONG, value.getLeastSignificantBits());
        }

        @Override
        public java.util.UUID read(NetworkBuffer buffer) {
            final long mostSignificantBits = buffer.read(LONG);
            final long leastSignificantBits = buffer.read(LONG);
            return new UUID(mostSignificantBits, leastSignificantBits);
        }
    }

    record PosType() implements Type<Pos> {
        @Override
        public void write(NetworkBuffer buffer, Pos value) {
            buffer.write(DOUBLE, value.x());
            buffer.write(DOUBLE, value.y());
            buffer.write(DOUBLE, value.z());
            buffer.write(FLOAT, value.yaw());
            buffer.write(FLOAT, value.pitch());
        }

        @Override
        public Pos read(NetworkBuffer buffer) {
            final double x = buffer.read(DOUBLE);
            final double y = buffer.read(DOUBLE);
            final double z = buffer.read(DOUBLE);
            final float yaw = buffer.read(FLOAT);
            final float pitch = buffer.read(FLOAT);
            return new Pos(x, y, z, yaw, pitch);
        }
    }

    record ByteArrayType() implements Type<byte[]> {
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
            Check.argCondition(length > remaining, "String is too long (length: {0}, readable: {1})", length, remaining);
            return buffer.read(FixedRawBytes(length));
        }
    }

    record LongArrayType() implements Type<long[]> {
        @Override
        public void write(NetworkBuffer buffer, long[] value) {
            buffer.write(VAR_INT, value.length);
            for (long l : value) buffer.write(LONG, l);
        }

        @Override
        public long[] read(NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
            final long[] longs = new long[length];
            for (int i = 0; i < length; i++) longs[i] = buffer.read(LONG);
            return longs;
        }
    }

    record VarIntArrayType() implements Type<int[]> {
        @Override
        public void write(NetworkBuffer buffer, int[] value) {
            buffer.write(VAR_INT, value.length);
            for (int i : value) buffer.write(VAR_INT, i);
        }

        @Override
        public int[] read(NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
            final int[] ints = new int[length];
            for (int i = 0; i < length; i++) ints[i] = buffer.read(VAR_INT);
            return ints;
        }
    }

    record VarLongArrayType() implements Type<long[]> {
        @Override
        public void write(NetworkBuffer buffer, long[] value) {
            buffer.write(VAR_INT, value.length);
            for (long l : value) buffer.write(VAR_LONG, l);
        }

        @Override
        public long[] read(NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
            final long[] longs = new long[length];
            for (int i = 0; i < length; i++) longs[i] = buffer.read(VAR_LONG);
            return longs;
        }
    }

    record Vector3Type() implements Type<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            buffer.write(FLOAT, (float) value.x());
            buffer.write(FLOAT, (float) value.y());
            buffer.write(FLOAT, (float) value.z());
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            final float x = buffer.read(FLOAT);
            final float y = buffer.read(FLOAT);
            final float z = buffer.read(FLOAT);
            return new Vec(x, y, z);
        }
    }

    record Vector3DType() implements Type<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            buffer.write(DOUBLE, value.x());
            buffer.write(DOUBLE, value.y());
            buffer.write(DOUBLE, value.z());
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            final double x = buffer.read(DOUBLE);
            final double y = buffer.read(DOUBLE);
            final double z = buffer.read(DOUBLE);
            return new Vec(x, y, z);
        }
    }

    record Vector3IType() implements Type<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            buffer.write(VAR_INT, (int) value.x());
            buffer.write(VAR_INT, (int) value.y());
            buffer.write(VAR_INT, (int) value.z());
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            final int x = buffer.read(VAR_INT);
            final int y = buffer.read(VAR_INT);
            final int z = buffer.read(VAR_INT);
            return new Vec(x, y, z);
        }
    }

    record Vector3BType() implements Type<Point> {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            buffer.write(BYTE, (byte) value.x());
            buffer.write(BYTE, (byte) value.y());
            buffer.write(BYTE, (byte) value.z());
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            final byte x = buffer.read(BYTE);
            final byte y = buffer.read(BYTE);
            final byte z = buffer.read(BYTE);
            return new Vec(x, y, z);
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
                if (hasContinuation)
                    buffer.write(VAR_INT, (int) (i >> 2));
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

    record QuaternionType() implements Type<float[]> {
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

    // Combinators

    record EnumSetType<E extends Enum<E>>(Class<E> enumType,
                                          E[] values) implements Type<EnumSet<E>> {
        public EnumSetType {
            Objects.requireNonNull(enumType, "enumType");
            Objects.requireNonNull(values, "values");
        }

        @Override
        public void write(NetworkBuffer buffer, EnumSet<E> value) {
            BitSet bitSet = new BitSet(values.length);
            for (int i = 0; i < values.length; ++i) {
                bitSet.set(i, value.contains(values[i]));
            }
            buffer.write(FixedBitSet(values.length), bitSet);
        }

        @Override
        public EnumSet<E> read(NetworkBuffer buffer) {
            final BitSet bitSet = buffer.read(FixedBitSet(values.length));
            EnumSet<E> enumSet = EnumSet.noneOf(enumType);
            for (int i = 0; i < values.length; ++i) {
                if (bitSet.get(i)) {
                    enumSet.add(values[i]);
                }
            }
            return enumSet;
        }
    }

    record FixedBitSetType(int length) implements Type<BitSet> {
        public FixedBitSetType {
            Check.argCondition(length < 0, "Length is negative found {0}", length);
        }

        @Override
        public void write(NetworkBuffer buffer, BitSet value) {
            if (value.length() > length) {
                throw new IllegalArgumentException("BitSet is larger than expected size (" + value.length() + ">" + length + ")");
            }
            final byte[] array = value.toByteArray();
            buffer.write(RAW_BYTES, Arrays.copyOf(array, (length + 7) / Long.BYTES));
        }

        @Override
        public BitSet read(NetworkBuffer buffer) {
            final byte[] array = buffer.read(FixedRawBytes((length + 7) / Long.BYTES));
            return BitSet.valueOf(array);
        }
    }

    record OptionalType<T extends @Nullable Object>(Type<T> parent) implements Type<T> {
        public OptionalType {
            Objects.requireNonNull(parent, "parent");
        }

        @Override
        public void write(NetworkBuffer buffer, @Nullable T value) {
            buffer.write(BOOLEAN, value != null);
            if (value != null) buffer.write(parent, value);
        }

        @Override
        public @Nullable T read(NetworkBuffer buffer) {
            return buffer.read(BOOLEAN) ? buffer.read(parent) : null;
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
            final Registries registries = buffer.registries();
            final byte[] componentData;
            if (registries != null) {
                componentData = NetworkBuffer.makeArray(parent, value, registries);
            } else {
                componentData = NetworkBuffer.makeArray(parent, value);
            }
            buffer.write(NetworkBuffer.BYTE_ARRAY, componentData);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
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
            if (buffer.read(BOOLEAN))
                return Either.left(buffer.read(left));
            return Either.right(buffer.read(right));
        }
    }

    record TransformType<T, S>(Type<T> parent, Function<T, S> to,
                               Function<S, T> from) implements Type<S> {
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
                buffer.write(BYTE, (byte) 0);
                return;
            }
            buffer.write(VAR_INT, values.size());
            for (T value : values) buffer.write(parent, value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<T> read(NetworkBuffer buffer) {
            final int size = buffer.read(VAR_INT);
            Check.argCondition(size > maxSize, "Collection size ({0}) is higher than the maximum allowed size ({1})", size, maxSize);
            T[] values = (T[]) new Object[size];
            for (int i = 0; i < size; i++) values[i] = buffer.read(parent);
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
                buffer.write(BYTE, (byte) 0);
                return;
            }
            buffer.write(VAR_INT, values.size());
            for (T value : values) buffer.write(parent, value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<T> read(NetworkBuffer buffer) {
            final int size = buffer.read(VAR_INT);
            Check.argCondition(size > maxSize, "Collection size ({0}) is higher than the maximum allowed size ({1})", size, maxSize);
            T[] values = (T[]) new Object[size];
            for (int i = 0; i < size; i++) values[i] = buffer.read(parent);
            return Set.of(values);
        }
    }

    record UnionType<T, K>(
            Type<K> keyType, Function<T, ? extends K> keyFunc,
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

            buffer.write(UNSIGNED_SHORT, utflen);
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
