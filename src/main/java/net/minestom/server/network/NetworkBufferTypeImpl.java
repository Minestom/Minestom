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
import net.minestom.server.utils.nbt.BinaryTagReader;
import net.minestom.server.utils.nbt.BinaryTagWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minestom.server.network.NetworkBuffer.*;
import static net.minestom.server.network.NetworkBufferImpl.impl;

interface NetworkBufferTypeImpl<T> extends NetworkBuffer.Type<T> {
    int SEGMENT_BITS = 0x7F;
    int CONTINUE_BIT = 0x80;

    record BooleanType() implements NetworkBufferTypeImpl<Boolean> {
        @Override
        public void write(NetworkBuffer buffer, Boolean value) {
            buffer.ensureWritable(1);
            impl(buffer)._putByte(buffer.writeIndex(), value ? (byte) 1 : (byte) 0);
            buffer.advanceWrite(1);
        }

        @Override
        public Boolean read(NetworkBuffer buffer) {
            final byte value = impl(buffer)._getByte(buffer.readIndex());
            buffer.advanceRead(1);
            return value == 1;
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
            final byte value = impl(buffer)._getByte(buffer.readIndex());
            buffer.advanceRead(1);
            return value;
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
            final byte value = impl(buffer)._getByte(buffer.readIndex());
            buffer.advanceRead(1);
            return (short) (value & 0xFF);
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
            final short value = impl(buffer)._getShort(buffer.readIndex());
            buffer.advanceRead(2);
            return value;
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
            final short value = impl(buffer)._getShort(buffer.readIndex());
            buffer.advanceRead(2);
            return value & 0xFFFF;
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
            final int value = impl(buffer)._getInt(buffer.readIndex());
            buffer.advanceRead(4);
            return value;
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
            final int value = impl(buffer)._getInt(buffer.readIndex());
            buffer.advanceRead(4);
            return value & 0xFFFFFFFFL;
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
            final long value = impl(buffer)._getLong(buffer.readIndex());
            buffer.advanceRead(8);
            return value;
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
            final float value = impl(buffer)._getFloat(buffer.readIndex());
            buffer.advanceRead(4);
            return value;
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
            final double value = impl(buffer)._getDouble(buffer.readIndex());
            buffer.advanceRead(8);
            return value;
        }
    }

    record VarIntType() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(NetworkBuffer buffer, Integer boxed) {
            buffer.ensureWritable(5);
            long index = buffer.writeIndex();
            int value = boxed;
            var nio = impl(buffer);
            while (true) {
                if ((value & ~SEGMENT_BITS) == 0) {
                    nio._putByte(index++, (byte) value);
                    buffer.advanceWrite(index - buffer.writeIndex());
                    return;
                }
                nio._putByte(index++, (byte) ((byte) (value & SEGMENT_BITS) | CONTINUE_BIT));
                // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
                value >>>= 7;
            }
        }

        @Override
        public Integer read(NetworkBuffer buffer) {
            long index = buffer.readIndex();
            // https://github.com/jvm-profiling-tools/async-profiler/blob/a38a375dc62b31a8109f3af97366a307abb0fe6f/src/converter/one/jfr/JfrReader.java#L393
            int result = 0;
            for (int shift = 0; shift <= 28; shift += 7) {
                byte b = impl(buffer)._getByte(index++);
                result |= (b & 0x7f) << shift;
                if (b >= 0) {
                    buffer.advanceRead(index - buffer.readIndex());
                    return result;
                }
            }
            throw new IndexOutOfBoundsException("VarInt too long");
        }
    }

    record OptionalVarIntType() implements NetworkBufferTypeImpl<@Nullable Integer> {
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
                impl(buffer)._putByte(buffer.writeIndex() + size, (byte) (value & SEGMENT_BITS | CONTINUE_BIT));
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
                currentByte = impl(buffer)._getByte(buffer.readIndex() + length);
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

    record RawBytesType(int length) implements NetworkBufferTypeImpl<byte[]> {
        @Override
        public void write(NetworkBuffer buffer, byte[] value) {
            if (length != -1 && value.length != length) {
                throw new IllegalArgumentException("Invalid length: " + value.length + " != " + length);
            }
            final int length = value.length;
            if (length == 0) return;
            buffer.ensureWritable(length);
            impl(buffer)._putBytes(buffer.writeIndex(), value);
            buffer.advanceWrite(length);
        }

        @Override
        public byte[] read(NetworkBuffer buffer) {
            long length = this.length;
            if (length == -1) {
                length = buffer.readableBytes();
            }
            if (length == 0) return new byte[0];
            assert length > 0 : "Invalid remaining: " + length;
            if (length > buffer.readableBytes())
                throw new IndexOutOfBoundsException("Buffer needs " + length + " bytes to read found" + buffer.readableBytes());
            final int arrayLength = Math.toIntExact(length);
            final byte[] bytes = new byte[arrayLength];
            impl(buffer)._getBytes(buffer.readIndex(), bytes);
            buffer.advanceRead(arrayLength);
            return bytes;
        }
    }

    record StringType() implements NetworkBufferTypeImpl<String> {
        @Override
        public void write(NetworkBuffer buffer, String value) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            buffer.write(BYTE_ARRAY, bytes);
        }

        @Override
        public String read(NetworkBuffer buffer) {
            final byte[] bytes = buffer.read(BYTE_ARRAY);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    record StringTerminatedType() implements NetworkBufferTypeImpl<String> {
        @Override
        public void write(NetworkBuffer buffer, String value) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            byte[] terminated = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, terminated, 0, bytes.length);
            terminated[terminated.length - 1] = 0;
            buffer.write(RAW_BYTES, terminated);
        }

        @Override
        public String read(NetworkBuffer buffer) {
            ByteArrayList bytes = new ByteArrayList();
            byte b;
            while ((b = buffer.read(BYTE)) != 0) {
                bytes.add(b);
            }
            return new String(bytes.elements(), StandardCharsets.UTF_8);
        }
    }

    record NbtType() implements NetworkBufferTypeImpl<BinaryTag> {
        @Override
        public void write(NetworkBuffer buffer, BinaryTag value) {
            BinaryTagWriter nbtWriter = new BinaryTagWriter(buffer.ioView());
            try {
                nbtWriter.writeNameless(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public BinaryTag read(NetworkBuffer buffer) {
            BinaryTagReader nbtReader = new BinaryTagReader(buffer.ioView());
            try {
                return nbtReader.readNameless();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    record JsonComponentType() implements NetworkBufferTypeImpl<Component> {
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
            Check.argCondition(length > remaining, "String is too long (length: {0}, readable: {1})", length, remaining);
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
            final int length = buffer.read(VAR_INT);
            final long[] longs = new long[length];
            for (int i = 0; i < length; i++) longs[i] = buffer.read(LONG);
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
            final int length = buffer.read(VAR_INT);
            final int[] ints = new int[length];
            for (int i = 0; i < length; i++) ints[i] = buffer.read(VAR_INT);
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
            final int length = buffer.read(VAR_INT);
            final long[] longs = new long[length];
            for (int i = 0; i < length; i++) longs[i] = buffer.read(VAR_LONG);
            return longs;
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

    // Combinators

    record EnumSetType<E extends Enum<E>>(Class<E> enumType,
                                          E[] values, Type<BitSet> bitSetType) implements Type<EnumSet<E>> {
        public EnumSetType {
            Objects.requireNonNull(enumType, "enumType");
            Objects.requireNonNull(values, "values");
            Objects.requireNonNull(bitSetType, "bitSetType");
        }

        public EnumSetType(Class<E> enumClass, E[] values) {
            this(enumClass, values, FixedBitSet(values.length));
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

        public FixedBitSetType(int length) {
            this(length, FixedRawBytes((length + 7) / Long.BYTES));
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
            Check.argCondition(length > maxLength, "Value is too long (length: {0}, max: {1})", length, maxLength);

            final long availableBytes = buffer.readableBytes();
            Check.argCondition(length > availableBytes, "Value is too long (length: {0}, available: {1})", length, availableBytes);
            final T value = parent.read(buffer);
            Check.argCondition(buffer.readableBytes() != availableBytes - length, "Value is too short (length: {0}, available: {1})", length, availableBytes);

            return value;
        }
    }

    record MaxLength<T>(Type<T> parent, long maxLength) implements NetworkBufferTypeImpl<T> {
        @Override
        public void write(NetworkBuffer buffer, T value) {
            final long length = parent.sizeOf(value);
            Check.argCondition(length > maxLength, "Value is too long (length: {0}, max: {1})", length, maxLength);
            buffer.write(parent, value);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            final long index = buffer.readIndex();
            final T value = parent.read(buffer);
            final long length = buffer.readIndex() - index;
            Check.argCondition(length > maxLength, "Value is too long (length: {0}, max: {1})", length, maxLength);
            return value;
        }
    }

    final class LazyType<T> implements NetworkBufferTypeImpl<T> {
        private final Supplier<NetworkBuffer.Type<T>> supplier;
        private Type<T> type;

        public LazyType(Supplier<NetworkBuffer.Type<T>> supplier) {
            this.supplier = supplier;
        }

        @Override
        public void write(NetworkBuffer buffer, T value) {
            if (type == null) type = supplier.get();
            type.write(buffer, value);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            if (type == null) type = supplier.get();
            return null;
        }
    }

    final class RecursiveType<T> implements NetworkBufferTypeImpl<T> {
        final Type<T> delegate;

        public RecursiveType(Function<Type<T>, Type<T>> self) {
            Objects.requireNonNull(self, "self");
            this.delegate = Objects.requireNonNull(self.apply(this), "delegate");
        }

        @Override
        public void write(NetworkBuffer buffer, T value) {
            delegate.write(buffer, value);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            return delegate.read(buffer);
        }
    }

    record TypedNbtType<T>(Codec<T> nbtType) implements NetworkBufferTypeImpl<T> {
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
            if (buffer.read(BOOLEAN))
                return Either.left(buffer.read(left));
            return Either.right(buffer.read(right));
        }
    }

    record TransformType<T, S>(Type<T> parent, Function<T, S> to,
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

    record SetType<T>(Type<T> parent, int maxSize) implements NetworkBufferTypeImpl<Set<T>> {
        @Override
        public void write(NetworkBuffer buffer, Set<T> values) {
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

    record UnionType<T, K, TR extends T>(
            Type<K> keyType, Function<T, ? extends K> keyFunc,
            Function<K, NetworkBuffer.Type<TR>> serializers
    ) implements NetworkBufferTypeImpl<T> {

        @SuppressWarnings("unchecked")
        // Much nicer than using the correct wildcard type for returns, pretty much ensuring T has subtypes already.
        @Override
        public void write(NetworkBuffer buffer, T value) {
            final K key = keyFunc.apply(value);
            buffer.write(keyType, key);
            var serializer = serializers.apply(key);
            if (serializer == null)
                throw new UnsupportedOperationException("Unrecognized type: " + key);
            serializer.write(buffer, (TR) value);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            final K key = buffer.read(keyType);
            var serializer = serializers.apply(key);
            if (serializer == null) throw new UnsupportedOperationException("Unrecognized type: " + key);
            return serializer.read(buffer);
        }
    }

    record TaggedType<T, D>(
            Type<D> discriminatorType, Function<? super T, ? extends D> discriminatorFromValue,
            Map<? super D, Type<? extends T>> serializerMap, @Nullable Type<? extends T> fallback
    ) implements NetworkBufferTypeImpl<T> {
        public TaggedType {
            Objects.requireNonNull(discriminatorType, "discriminatorType");
            Objects.requireNonNull(discriminatorFromValue, "discriminatorFromValue");
            serializerMap = Map.copyOf(serializerMap);
        }

        @SuppressWarnings("unchecked") // Likely fine here
        @Override
        public void write(NetworkBuffer buffer, T value) {
            final D key = discriminatorFromValue.apply(value);
            buffer.write(discriminatorType, key);
            var serializer = serializerMap.getOrDefault(key, fallback);
            if (serializer == null)
                throw new UnsupportedOperationException("Unrecognized type: " + key);
            ((Type<T>) serializer).write(buffer, value);
        }

        @Override
        public T read(NetworkBuffer buffer) {
            final D key = buffer.read(discriminatorType);
            var serializer = serializerMap.getOrDefault(key, fallback);
            if (serializer == null) throw new UnsupportedOperationException("Unrecognized type: " + key);
            return serializer.read(buffer);
        }
    }

    /**
     * This is a very gross version of {@link java.io.DataOutputStream#writeUTF(String)} & ${@link DataInputStream#readUTF()}. We need the data in the java
     * modified utf-8 format for Component, and I couldnt find a method without creating a new buffer for it.
     */
    record IOUTF8StringType() implements NetworkBufferTypeImpl<String> {
        @Override
        public void write(NetworkBuffer buffer, String value) {
            final int strlen = value.length();
            int utflen = strlen; // optimized for ASCII

            for (int i = 0; i < strlen; i++) {
                int c = value.charAt(i);
                if (c >= 0x80 || c == 0)
                    utflen += (c >= 0x800) ? 2 : 1;
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
            int utflen = buffer.read(UNSIGNED_SHORT);
            if (buffer.readableBytes() < utflen) throw new IllegalArgumentException("Invalid String size.");
            byte[] bytearr = buffer.read(FixedRawBytes(utflen));
            final char[] chararr = new char[utflen];

            int c, char2, char3;
            int count = 0;
            int chararr_count = 0;

            while (count < utflen) {
                c = (int) bytearr[count] & 0xff;
                if (c > 127) break;
                count++;
                chararr[chararr_count++] = (char) c;
            }

            while (count < utflen) {
                c = (int) bytearr[count] & 0xff;
                try { // Surround in try catch to throw a runtime exception instead of a checked one
                    switch (c >> 4) {
                        case 0, 1, 2, 3, 4, 5, 6, 7 -> {
                            /* 0xxxxxxx*/
                            count++;
                            chararr[chararr_count++] = (char) c;
                        }
                        case 12, 13 -> {
                            /* 110x xxxx   10xx xxxx*/
                            count += 2;
                            if (count > utflen)
                                throw new UTFDataFormatException(
                                        "malformed input: partial character at end");
                            char2 = bytearr[count - 1];
                            if ((char2 & 0xC0) != 0x80)
                                throw new UTFDataFormatException(
                                        "malformed input around byte " + count);
                            chararr[chararr_count++] = (char) (((c & 0x1F) << 6) |
                                    (char2 & 0x3F));
                        }
                        case 14 -> {
                            /* 1110 xxxx  10xx xxxx  10xx xxxx */
                            count += 3;
                            if (count > utflen)
                                throw new UTFDataFormatException(
                                        "malformed input: partial character at end");
                            char2 = bytearr[count - 2];
                            char3 = bytearr[count - 1];
                            if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                                throw new UTFDataFormatException(
                                        "malformed input around byte " + (count - 1));
                            chararr[chararr_count++] = (char) (((c & 0x0F) << 12) |
                                    ((char2 & 0x3F) << 6) |
                                    ((char3 & 0x3F) << 0));
                        }
                        default ->
                            /* 10xx xxxx,  1111 xxxx */
                                throw new UTFDataFormatException(
                                        "malformed input around byte " + count);
                    }
                } catch (UTFDataFormatException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            // The number of chars produced may be less than utflen
            return new String(chararr, 0, chararr_count);
        }
    }

    Type<Unit> UNIT = NetworkBufferTemplate.template(Unit.INSTANCE);

    Type<UUID> UUID = NetworkBufferTemplate.template(
            LONG, java.util.UUID::getMostSignificantBits,
            LONG, java.util.UUID::getLeastSignificantBits,
            UUID::new
    );

    Type<Pos> POS = NetworkBufferTemplate.template(
            DOUBLE, Pos::x,
            DOUBLE, Pos::y,
            DOUBLE, Pos::z,
            FLOAT, Pos::yaw,
            FLOAT, Pos::pitch,
            Pos::new
    );

    Type<Point> VECTOR3 = NetworkBufferTemplate.template(
            FLOAT, p -> (float) p.x(),
            FLOAT, p -> (float) p.y(),
            FLOAT, p -> (float) p.z(),
            Vec::new
    );

    Type<Point> VECTOR3D = NetworkBufferTemplate.template(
            DOUBLE, Point::x,
            DOUBLE, Point::y,
            DOUBLE, Point::z,
            Vec::new
    );

    Type<Point> VECTOR3I = NetworkBufferTemplate.template(
            VAR_INT, p -> (int) p.x(),
            VAR_INT, p -> (int) p.y(),
            VAR_INT, p -> (int) p.z(),
            Vec::new
    );

    Type<Point> VECTOR3B = NetworkBufferTemplate.template(
            BYTE, p -> (byte) p.x(),
            BYTE, p -> (byte) p.y(),
            BYTE, p -> (byte) p.z(),
            Vec::new
    );

    Type<float[]> QUATERNION = NetworkBufferTemplate.template(
            FLOAT, arr -> arr[0],
            FLOAT, arr -> arr[1],
            FLOAT, arr -> arr[2],
            FLOAT, arr -> arr[3],
            (a, b, c, d) -> new float[]{a, b, c, d}
    );

    Type<Point> BLOCK_POSITION = LONG.transform(
            value -> new Vec((int) (value >> 38), (int) (value << 52 >> 52), (int) (value << 26 >> 38)),
            p -> (((long) p.blockX() & 0x3FFFFFF) << 38) |
                    (((long) p.blockZ() & 0x3FFFFFF) << 12) |
                    ((long) p.blockY() & 0xFFF)
    );

    static <T> long sizeOf(Type<T> type, T value, Registries registries) {
        NetworkBuffer buffer = NetworkBufferImpl.dummy(registries);
        type.write(buffer, value);
        return buffer.writeIndex();
    }
}
