package net.minestom.server.network;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.nbt.BinaryTagReader;
import net.minestom.server.utils.nbt.BinaryTagWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minestom.server.network.NetworkBuffer.*;
import static net.minestom.server.network.NetworkBufferImpl.impl;

interface NetworkBufferTypeImpl<T> extends NetworkBuffer.Type<T> {
    int SEGMENT_BITS = 0x7F;
    int CONTINUE_BIT = 0x80;

    record UnitType() implements NetworkBufferTypeImpl<Unit> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Unit value) {
        }

        @Override
        public Unit read(@NotNull NetworkBuffer buffer) {
            return Unit.INSTANCE;
        }
    }

    record BooleanType() implements NetworkBufferTypeImpl<Boolean> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Boolean value) {
            buffer.ensureSize(1);
            impl(buffer).nioBuffer.put(buffer.writeIndex(), value ? (byte) 1 : (byte) 0);
            buffer.advanceWrite(1);
        }

        @Override
        public Boolean read(@NotNull NetworkBuffer buffer) {
            final byte value = impl(buffer).nioBuffer.get(buffer.readIndex());
            buffer.advanceRead(1);
            return value == 1;
        }
    }

    record ByteType() implements NetworkBufferTypeImpl<Byte> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Byte value) {
            buffer.ensureSize(1);
            impl(buffer).nioBuffer.put(buffer.writeIndex(), value);
            buffer.advanceWrite(1);
        }

        @Override
        public Byte read(@NotNull NetworkBuffer buffer) {
            final byte value = impl(buffer).nioBuffer.get(buffer.readIndex());
            buffer.advanceRead(1);
            return value;
        }
    }

    record ShortType() implements NetworkBufferTypeImpl<Short> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Short value) {
            buffer.ensureSize(2);
            impl(buffer).nioBuffer.putShort(buffer.writeIndex(), value);
            buffer.advanceWrite(2);
        }

        @Override
        public Short read(@NotNull NetworkBuffer buffer) {
            final short value = impl(buffer).nioBuffer.getShort(buffer.readIndex());
            buffer.advanceRead(2);
            return value;
        }
    }

    record UnsignedShortType() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Integer value) {
            buffer.ensureSize(2);
            impl(buffer).nioBuffer.putShort(buffer.writeIndex(), (short) (value & 0xFFFF));
            buffer.advanceWrite(2);
        }

        @Override
        public Integer read(@NotNull NetworkBuffer buffer) {
            final short value = impl(buffer).nioBuffer.getShort(buffer.readIndex());
            buffer.advanceRead(2);
            return value & 0xFFFF;
        }
    }

    record IntType() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Integer value) {
            buffer.ensureSize(4);
            impl(buffer).nioBuffer.putInt(buffer.writeIndex(), value);
            buffer.advanceWrite(4);
        }

        @Override
        public Integer read(@NotNull NetworkBuffer buffer) {
            final int value = impl(buffer).nioBuffer.getInt(buffer.readIndex());
            buffer.advanceRead(4);
            return value;
        }
    }

    record LongType() implements NetworkBufferTypeImpl<Long> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Long value) {
            buffer.ensureSize(8);
            impl(buffer).nioBuffer.putLong(buffer.writeIndex(), value);
            buffer.advanceWrite(8);
        }

        @Override
        public Long read(@NotNull NetworkBuffer buffer) {
            final long value = impl(buffer).nioBuffer.getLong(buffer.readIndex());
            buffer.advanceRead(8);
            return value;
        }
    }

    record FloatType() implements NetworkBufferTypeImpl<Float> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Float value) {
            buffer.ensureSize(4);
            impl(buffer).nioBuffer.putFloat(buffer.writeIndex(), value);
            buffer.advanceWrite(4);
        }

        @Override
        public Float read(@NotNull NetworkBuffer buffer) {
            final float value = impl(buffer).nioBuffer.getFloat(buffer.readIndex());
            buffer.advanceRead(4);
            return value;
        }
    }

    record DoubleType() implements NetworkBufferTypeImpl<Double> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Double value) {
            buffer.ensureSize(8);
            impl(buffer).nioBuffer.putDouble(buffer.writeIndex(), value);
            buffer.advanceWrite(8);
        }

        @Override
        public Double read(@NotNull NetworkBuffer buffer) {
            final double value = impl(buffer).nioBuffer.getDouble(buffer.readIndex());
            buffer.advanceRead(8);
            return value;
        }
    }

    record VarIntType() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Integer boxed) {
            final int value = boxed;
            final int index = buffer.writeIndex();
            if ((value & (0xFFFFFFFF << 7)) == 0) {
                buffer.ensureSize(1);
                var nio = impl(buffer).nioBuffer;
                nio.put(index, (byte) value);
                buffer.advanceWrite(1);
            } else if ((value & (0xFFFFFFFF << 14)) == 0) {
                buffer.ensureSize(2);
                var nio = impl(buffer).nioBuffer;
                nio.putShort(index, (short) ((value & 0x7F | 0x80) << 8 | (value >>> 7)));
                buffer.advanceWrite(2);
            } else if ((value & (0xFFFFFFFF << 21)) == 0) {
                buffer.ensureSize(3);
                var nio = impl(buffer).nioBuffer;
                nio.put(index, (byte) (value & 0x7F | 0x80));
                nio.put(index + 1, (byte) ((value >>> 7) & 0x7F | 0x80));
                nio.put(index + 2, (byte) (value >>> 14));
                buffer.advanceWrite(3);
            } else if ((value & (0xFFFFFFFF << 28)) == 0) {
                buffer.ensureSize(4);
                var nio = impl(buffer).nioBuffer;
                nio.putInt(index, (value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                        | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21));
                buffer.advanceWrite(4);
            } else {
                buffer.ensureSize(5);
                var nio = impl(buffer).nioBuffer;
                nio.putInt(index, (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                        | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80));
                nio.put(index + 4, (byte) (value >>> 28));
                buffer.advanceWrite(5);
            }
        }

        @Override
        public Integer read(@NotNull NetworkBuffer buffer) {
            int index = buffer.readIndex();
            // https://github.com/jvm-profiling-tools/async-profiler/blob/a38a375dc62b31a8109f3af97366a307abb0fe6f/src/converter/one/jfr/JfrReader.java#L393
            int result = 0;
            for (int shift = 0; ; shift += 7) {
                byte b = impl(buffer).nioBuffer.get(index++);
                result |= (b & 0x7f) << shift;
                if (b >= 0) {
                    buffer.advanceRead(index - buffer.readIndex());
                    return result;
                }
            }
        }
    }

    record VarLongType() implements NetworkBufferTypeImpl<Long> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Long value) {
            buffer.ensureSize(10);
            int size = 0;
            while (true) {
                if ((value & ~((long) SEGMENT_BITS)) == 0) {
                    impl(buffer).nioBuffer.put(buffer.writeIndex() + size, (byte) value.intValue());
                    buffer.advanceWrite(size + 1);
                    return;
                }
                impl(buffer).nioBuffer.put(buffer.writeIndex() + size, (byte) (value & SEGMENT_BITS | CONTINUE_BIT));
                size++;
                // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
                value >>>= 7;
            }
        }

        @Override
        public Long read(@NotNull NetworkBuffer buffer) {
            int length = 0;
            long value = 0;
            int position = 0;
            byte currentByte;
            while (true) {
                currentByte = impl(buffer).nioBuffer.get(buffer.readIndex() + length);
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
        public void write(@NotNull NetworkBuffer buffer, byte[] value) {
            if (length != -1 && value.length != length) {
                throw new IllegalArgumentException("Invalid length: " + value.length + " != " + length);
            }
            buffer.ensureSize(value.length);
            impl(buffer).nioBuffer.put(buffer.writeIndex(), value);
            buffer.advanceWrite(value.length);
        }

        @Override
        public byte[] read(@NotNull NetworkBuffer buffer) {
            int length = buffer.readableBytes();
            if (this.length != -1) {
                length = Math.min(length, this.length);
            }
            if (length == 0) return new byte[0];
            assert length > 0 : "Invalid remaining: " + length;
            final byte[] bytes = new byte[length];
            impl(buffer).nioBuffer.get(buffer.readIndex(), bytes);
            buffer.advanceRead(length);
            return bytes;
        }
    }

    record StringType() implements NetworkBufferTypeImpl<String> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, String value) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            buffer.write(BYTE_ARRAY, bytes);
        }

        @Override
        public String read(@NotNull NetworkBuffer buffer) {
            final byte[] bytes = buffer.read(BYTE_ARRAY);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    record StringTerminatedType() implements NetworkBufferTypeImpl<String> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, String value) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            byte[] terminated = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, terminated, 0, bytes.length);
            terminated[terminated.length - 1] = 0;
            buffer.write(RAW_BYTES, terminated);
        }

        @Override
        public String read(@NotNull NetworkBuffer buffer) {
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
        public void write(@NotNull NetworkBuffer buffer, BinaryTag value) {
            BinaryTagWriter nbtWriter = impl(buffer).nbtWriter;
            if (nbtWriter == null) {
                nbtWriter = new BinaryTagWriter(new DataOutputStream(new OutputStream() {
                    @Override
                    public void write(int b) {
                        buffer.write(BYTE, (byte) b);
                    }
                }));
                impl(buffer).nbtWriter = nbtWriter;
            }
            try {
                nbtWriter.writeNameless(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public BinaryTag read(@NotNull NetworkBuffer buffer) {
            BinaryTagReader nbtReader = impl(buffer).nbtReader;
            if (nbtReader == null) {
                nbtReader = new BinaryTagReader(new DataInputStream(new InputStream() {
                    @Override
                    public int read() {
                        return buffer.read(BYTE) & 0xFF;
                    }

                    @Override
                    public int available() {
                        return buffer.readableBytes();
                    }
                }));
                impl(buffer).nbtReader = nbtReader;
            }
            try {
                return nbtReader.readNameless();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    record BlockPositionType() implements NetworkBufferTypeImpl<Point> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Point value) {
            final int blockX = value.blockX();
            final int blockY = value.blockY();
            final int blockZ = value.blockZ();
            final long longPos = (((long) blockX & 0x3FFFFFF) << 38) |
                    (((long) blockZ & 0x3FFFFFF) << 12) |
                    ((long) blockY & 0xFFF);
            buffer.write(LONG, longPos);
        }

        @Override
        public Point read(@NotNull NetworkBuffer buffer) {
            final long value = buffer.read(LONG);
            final int x = (int) (value >> 38);
            final int y = (int) (value << 52 >> 52);
            final int z = (int) (value << 26 >> 38);
            return new Vec(x, y, z);
        }
    }

    record ComponentType() implements NetworkBufferTypeImpl<Component> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Component value) {
            final BinaryTag nbt = NbtComponentSerializer.nbt().serialize(value);
            buffer.write(NBT, nbt);
        }

        @Override
        public Component read(@NotNull NetworkBuffer buffer) {
            final BinaryTag nbt = buffer.read(NBT);
            return NbtComponentSerializer.nbt().deserialize(nbt);
        }
    }

    record JsonComponentType() implements NetworkBufferTypeImpl<Component> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Component value) {
            final String json = GsonComponentSerializer.gson().serialize(value);
            buffer.write(STRING, json);
        }

        @Override
        public Component read(@NotNull NetworkBuffer buffer) {
            final String json = buffer.read(STRING);
            return GsonComponentSerializer.gson().deserialize(json);
        }
    }

    record UUIDType() implements NetworkBufferTypeImpl<UUID> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, java.util.UUID value) {
            buffer.write(LONG, value.getMostSignificantBits());
            buffer.write(LONG, value.getLeastSignificantBits());
        }

        @Override
        public java.util.UUID read(@NotNull NetworkBuffer buffer) {
            final long mostSignificantBits = buffer.read(LONG);
            final long leastSignificantBits = buffer.read(LONG);
            return new UUID(mostSignificantBits, leastSignificantBits);
        }
    }

    record PosType() implements NetworkBufferTypeImpl<Pos> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Pos value) {
            buffer.write(DOUBLE, value.x());
            buffer.write(DOUBLE, value.y());
            buffer.write(DOUBLE, value.z());
            buffer.write(FLOAT, value.yaw());
            buffer.write(FLOAT, value.pitch());
        }

        @Override
        public Pos read(@NotNull NetworkBuffer buffer) {
            final double x = buffer.read(DOUBLE);
            final double y = buffer.read(DOUBLE);
            final double z = buffer.read(DOUBLE);
            final float yaw = buffer.read(FLOAT);
            final float pitch = buffer.read(FLOAT);
            return new Pos(x, y, z, yaw, pitch);
        }
    }

    record ByteArrayType() implements NetworkBufferTypeImpl<byte[]> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, byte[] value) {
            buffer.write(VAR_INT, value.length);
            buffer.write(RAW_BYTES, value);
        }

        @Override
        public byte[] read(@NotNull NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
            if (length == 0) return new byte[0];
            final int remaining = buffer.size();
            Check.argCondition(length > remaining, "String is too long (length: {0}, readable: {1})", length, remaining);
            return buffer.read(FixedRawBytes(length));
        }
    }

    record LongArrayType() implements NetworkBufferTypeImpl<long[]> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, long[] value) {
            buffer.write(VAR_INT, value.length);
            for (long l : value) buffer.write(LONG, l);
        }

        @Override
        public long[] read(@NotNull NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
            final long[] longs = new long[length];
            for (int i = 0; i < length; i++) longs[i] = buffer.read(LONG);
            return longs;
        }
    }

    record VarIntArrayType() implements NetworkBufferTypeImpl<int[]> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, int[] value) {
            buffer.write(VAR_INT, value.length);
            for (int i : value) buffer.write(VAR_INT, i);
        }

        @Override
        public int[] read(@NotNull NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
            final int[] ints = new int[length];
            for (int i = 0; i < length; i++) ints[i] = buffer.read(VAR_INT);
            return ints;
        }
    }

    record VarLongArrayType() implements NetworkBufferTypeImpl<long[]> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, long[] value) {
            buffer.write(VAR_INT, value.length);
            for (long l : value) buffer.write(VAR_LONG, l);
        }

        @Override
        public long[] read(@NotNull NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
            final long[] longs = new long[length];
            for (int i = 0; i < length; i++) longs[i] = buffer.read(VAR_LONG);
            return longs;
        }
    }

    // METADATA

    record BlockStateType() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Integer value) {
            buffer.write(VAR_INT, value);
        }

        @Override
        public Integer read(@NotNull NetworkBuffer buffer) {
            return buffer.read(VAR_INT);
        }
    }

    record VillagerDataType() implements NetworkBufferTypeImpl<int[]> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, int[] value) {
            buffer.write(VAR_INT, value[0]);
            buffer.write(VAR_INT, value[1]);
            buffer.write(VAR_INT, value[2]);
        }

        @Override
        public int[] read(@NotNull NetworkBuffer buffer) {
            final int[] value = new int[3];
            value[0] = buffer.read(VAR_INT);
            value[1] = buffer.read(VAR_INT);
            value[2] = buffer.read(VAR_INT);
            return value;
        }
    }

    record Vector3Type() implements NetworkBufferTypeImpl<Point> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Point value) {
            buffer.write(FLOAT, (float) value.x());
            buffer.write(FLOAT, (float) value.y());
            buffer.write(FLOAT, (float) value.z());
        }

        @Override
        public Point read(@NotNull NetworkBuffer buffer) {
            final float x = buffer.read(FLOAT);
            final float y = buffer.read(FLOAT);
            final float z = buffer.read(FLOAT);
            return new Vec(x, y, z);
        }
    }

    record Vector3DType() implements NetworkBufferTypeImpl<Point> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Point value) {
            buffer.write(DOUBLE, value.x());
            buffer.write(DOUBLE, value.y());
            buffer.write(DOUBLE, value.z());
        }

        @Override
        public Point read(@NotNull NetworkBuffer buffer) {
            final double x = buffer.read(DOUBLE);
            final double y = buffer.read(DOUBLE);
            final double z = buffer.read(DOUBLE);
            return new Vec(x, y, z);
        }
    }

    record Vector3BType() implements NetworkBufferTypeImpl<Point> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Point value) {
            buffer.write(BYTE, (byte) value.x());
            buffer.write(BYTE, (byte) value.y());
            buffer.write(BYTE, (byte) value.z());
        }

        @Override
        public Point read(@NotNull NetworkBuffer buffer) {
            final byte x = buffer.read(BYTE);
            final byte y = buffer.read(BYTE);
            final byte z = buffer.read(BYTE);
            return new Vec(x, y, z);
        }
    }

    record QuaternionType() implements NetworkBufferTypeImpl<float[]> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, float[] value) {
            buffer.write(FLOAT, value[0]);
            buffer.write(FLOAT, value[1]);
            buffer.write(FLOAT, value[2]);
            buffer.write(FLOAT, value[3]);
        }

        @Override
        public float[] read(@NotNull NetworkBuffer buffer) {
            final float x = buffer.read(FLOAT);
            final float y = buffer.read(FLOAT);
            final float z = buffer.read(FLOAT);
            final float w = buffer.read(FLOAT);
            return new float[]{x, y, z, w};
        }
    }

    // Combinators

    record EnumSetType<E extends Enum<E>>(@NotNull Class<E> enumType,
                                          E[] values) implements NetworkBufferTypeImpl<EnumSet<E>> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, EnumSet<E> value) {
            BitSet bitSet = new BitSet(values.length);
            for (int i = 0; i < values.length; ++i) {
                bitSet.set(i, value.contains(values[i]));
            }
            final byte[] array = bitSet.toByteArray();
            buffer.write(RAW_BYTES, array);
        }

        @Override
        public EnumSet<E> read(@NotNull NetworkBuffer buffer) {
            final byte[] array = buffer.read(FixedRawBytes((values.length + 7) / 8));
            BitSet bitSet = BitSet.valueOf(array);
            EnumSet<E> enumSet = EnumSet.noneOf(enumType);
            for (int i = 0; i < values.length; ++i) {
                if (bitSet.get(i)) {
                    enumSet.add(values[i]);
                }
            }
            return enumSet;
        }
    }

    record FixedBitSetType(int length) implements NetworkBufferTypeImpl<BitSet> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, BitSet value) {
            final int setLength = value.length();
            if (setLength > length) {
                throw new IllegalArgumentException("BitSet is larger than expected size (" + setLength + ">" + length + ")");
            } else {
                final byte[] array = value.toByteArray();
                buffer.write(RAW_BYTES, array);
            }
        }

        @Override
        public BitSet read(@NotNull NetworkBuffer buffer) {
            final byte[] array = buffer.read(FixedRawBytes((length + 7) / 8));
            return BitSet.valueOf(array);
        }
    }

    record OptionalType<T>(@NotNull Type<T> parent) implements NetworkBufferTypeImpl<@Nullable T> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, T value) {
            buffer.write(BOOLEAN, value != null);
            if (value != null) buffer.write(parent, value);
        }

        @Override
        public T read(@NotNull NetworkBuffer buffer) {
            return buffer.read(BOOLEAN) ? buffer.read(parent) : null;
        }
    }

    final class LazyType<T> implements NetworkBufferTypeImpl<T> {
        private final @NotNull Supplier<NetworkBuffer.@NotNull Type<T>> supplier;
        private Type<T> type;

        public LazyType(@NotNull Supplier<NetworkBuffer.@NotNull Type<T>> supplier) {
            this.supplier = supplier;
        }

        @Override
        public void write(@NotNull NetworkBuffer buffer, T value) {
            if (type == null) type = supplier.get();
            type.write(buffer, value);
        }

        @Override
        public T read(@NotNull NetworkBuffer buffer) {
            if (type == null) type = supplier.get();
            return null;
        }
    }

    record TransformType<T, S>(@NotNull Type<T> parent, @NotNull Function<T, S> to,
                               @NotNull Function<S, T> from) implements NetworkBufferTypeImpl<S> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, S value) {
            parent.write(buffer, from.apply(value));
        }

        @Override
        public S read(@NotNull NetworkBuffer buffer) {
            return to.apply(parent.read(buffer));
        }
    }

    record MapType<K, V>(@NotNull Type<K> parent, @NotNull NetworkBuffer.Type<V> valueType,
                         int maxSize) implements NetworkBufferTypeImpl<Map<K, V>> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Map<K, V> map) {
            buffer.write(VAR_INT, map.size());
            for (Map.Entry<K, V> entry : map.entrySet()) {
                buffer.write(parent, entry.getKey());
                buffer.write(valueType, entry.getValue());
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<K, V> read(@NotNull NetworkBuffer buffer) {
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

    record ListType<T>(@NotNull Type<T> parent, int maxSize) implements NetworkBufferTypeImpl<List<T>> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, List<T> values) {
            if (values == null) {
                buffer.write(BYTE, (byte) 0);
                return;
            }
            buffer.write(VAR_INT, values.size());
            for (T value : values) buffer.write(parent, value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<T> read(@NotNull NetworkBuffer buffer) {
            final int size = buffer.read(VAR_INT);
            Check.argCondition(size > maxSize, "Collection size ({0}) is higher than the maximum allowed size ({1})", size, maxSize);
            T[] values = (T[]) new Object[size];
            for (int i = 0; i < size; i++) values[i] = buffer.read(parent);
            return List.of(values);
        }
    }

    record RegistryTypeType<T extends ProtocolObject>(
            @NotNull Function<Registries, DynamicRegistry<T>> selector) implements NetworkBufferTypeImpl<DynamicRegistry.Key<T>> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, DynamicRegistry.Key<T> value) {
            final Registries registries = impl(buffer).registries;
            Check.stateCondition(registries == null, "Buffer does not have registries");
            final DynamicRegistry<T> registry = selector.apply(registries);
            final int id = registry.getId(value);
            Check.argCondition(id == -1, "Key is not registered: {0} > {1}", registry, value);
            buffer.write(VAR_INT, id);
        }

        @Override
        public DynamicRegistry.Key<T> read(@NotNull NetworkBuffer buffer) {
            final Registries registries = impl(buffer).registries;
            Check.stateCondition(registries == null, "Buffer does not have registries");
            DynamicRegistry<T> registry = selector.apply(registries);
            final int id = buffer.read(VAR_INT);
            final DynamicRegistry.Key<T> key = registry.getKey(id);
            Check.argCondition(key == null, "No such ID in registry: {0} > {1}", registry, id);
            return key;
        }
    }
}
