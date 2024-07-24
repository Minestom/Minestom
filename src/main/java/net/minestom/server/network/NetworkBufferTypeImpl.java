package net.minestom.server.network;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.data.WorldPos;
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
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minestom.server.network.NetworkBuffer.*;

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
            buffer.nioBuffer.put(buffer.writeIndex(), value ? (byte) 1 : (byte) 0);
            buffer.writeIndex += 1;
        }

        @Override
        public Boolean read(@NotNull NetworkBuffer buffer) {
            final byte value = buffer.nioBuffer.get(buffer.readIndex());
            buffer.readIndex += 1;
            return value == 1;
        }
    }

    record ByteType() implements NetworkBufferTypeImpl<Byte> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Byte value) {
            buffer.ensureSize(1);
            buffer.nioBuffer.put(buffer.writeIndex(), value);
            buffer.writeIndex += 1;
        }

        @Override
        public Byte read(@NotNull NetworkBuffer buffer) {
            final byte value = buffer.nioBuffer.get(buffer.readIndex());
            buffer.readIndex += 1;
            return value;
        }
    }

    record ShortType() implements NetworkBufferTypeImpl<Short> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Short value) {
            buffer.ensureSize(2);
            buffer.nioBuffer.putShort(buffer.writeIndex(), value);
            buffer.writeIndex += 2;
        }

        @Override
        public Short read(@NotNull NetworkBuffer buffer) {
            final short value = buffer.nioBuffer.getShort(buffer.readIndex());
            buffer.readIndex += 2;
            return value;
        }
    }

    record UnsignedShortType() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Integer value) {
            buffer.ensureSize(2);
            buffer.nioBuffer.putShort(buffer.writeIndex(), (short) (value & 0xFFFF));
            buffer.writeIndex += 2;
        }

        @Override
        public Integer read(@NotNull NetworkBuffer buffer) {
            final short value = buffer.nioBuffer.getShort(buffer.readIndex());
            buffer.readIndex += 2;
            return value & 0xFFFF;
        }
    }

    record IntType() implements NetworkBufferTypeImpl<Integer> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Integer value) {
            buffer.ensureSize(4);
            buffer.nioBuffer.putInt(buffer.writeIndex(), value);
            buffer.writeIndex += 4;
        }

        @Override
        public Integer read(@NotNull NetworkBuffer buffer) {
            final int value = buffer.nioBuffer.getInt(buffer.readIndex());
            buffer.readIndex += 4;
            return value;
        }
    }

    record LongType() implements NetworkBufferTypeImpl<Long> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Long value) {
            buffer.ensureSize(8);
            buffer.nioBuffer.putLong(buffer.writeIndex(), value);
            buffer.writeIndex += 8;
        }

        @Override
        public Long read(@NotNull NetworkBuffer buffer) {
            final long value = buffer.nioBuffer.getLong(buffer.readIndex());
            buffer.readIndex += 8;
            return value;
        }
    }

    record FloatType() implements NetworkBufferTypeImpl<Float> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Float value) {
            buffer.ensureSize(4);
            buffer.nioBuffer.putFloat(buffer.writeIndex(), value);
            buffer.writeIndex += 4;
        }

        @Override
        public Float read(@NotNull NetworkBuffer buffer) {
            final float value = buffer.nioBuffer.getFloat(buffer.readIndex());
            buffer.readIndex += 4;
            return value;
        }
    }

    record DoubleType() implements NetworkBufferTypeImpl<Double> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Double value) {
            buffer.ensureSize(8);
            buffer.nioBuffer.putDouble(buffer.writeIndex(), value);
            buffer.writeIndex += 8;
        }

        @Override
        public Double read(@NotNull NetworkBuffer buffer) {
            final double value = buffer.nioBuffer.getDouble(buffer.readIndex());
            buffer.readIndex += 8;
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
                buffer.nioBuffer.put(index, (byte) value);
                buffer.writeIndex += 1;
            } else if ((value & (0xFFFFFFFF << 14)) == 0) {
                buffer.ensureSize(2);
                buffer.nioBuffer.putShort(index, (short) ((value & 0x7F | 0x80) << 8 | (value >>> 7)));
                buffer.writeIndex += 2;
            } else if ((value & (0xFFFFFFFF << 21)) == 0) {
                buffer.ensureSize(3);
                var nio = buffer.nioBuffer;
                nio.put(index, (byte) (value & 0x7F | 0x80));
                nio.put(index + 1, (byte) ((value >>> 7) & 0x7F | 0x80));
                nio.put(index + 2, (byte) (value >>> 14));
                buffer.writeIndex += 3;
            } else if ((value & (0xFFFFFFFF << 28)) == 0) {
                buffer.ensureSize(4);
                var nio = buffer.nioBuffer;
                nio.putInt(index, (value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                        | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21));
                buffer.writeIndex += 4;
            } else {
                buffer.ensureSize(5);
                var nio = buffer.nioBuffer;
                nio.putInt(index, (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                        | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80));
                nio.put(index + 4, (byte) (value >>> 28));
                buffer.writeIndex += 5;
            }
        }

        @Override
        public Integer read(@NotNull NetworkBuffer buffer) {
            int index = buffer.readIndex();
            // https://github.com/jvm-profiling-tools/async-profiler/blob/a38a375dc62b31a8109f3af97366a307abb0fe6f/src/converter/one/jfr/JfrReader.java#L393
            int result = 0;
            for (int shift = 0; ; shift += 7) {
                byte b = buffer.nioBuffer.get(index++);
                result |= (b & 0x7f) << shift;
                if (b >= 0) {
                    buffer.readIndex += index - buffer.readIndex();
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
                    buffer.nioBuffer.put(buffer.writeIndex() + size, (byte) value.intValue());
                    buffer.writeIndex += size + 1;
                    return;
                }
                buffer.nioBuffer.put(buffer.writeIndex() + size, (byte) (value & SEGMENT_BITS | CONTINUE_BIT));
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
                currentByte = buffer.nioBuffer.get(buffer.readIndex() + length);
                length++;
                value |= (long) (currentByte & SEGMENT_BITS) << position;
                if ((currentByte & CONTINUE_BIT) == 0) break;
                position += 7;
                if (position >= 64) throw new RuntimeException("VarLong is too big");
            }
            buffer.readIndex += length;
            return value;
        }
    }

    record RawBytesType() implements NetworkBufferTypeImpl<byte[]> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, byte[] value) {
            buffer.ensureSize(value.length);
            buffer.nioBuffer.put(buffer.writeIndex(), value);
            buffer.writeIndex += value.length;
        }

        @Override
        public byte[] read(@NotNull NetworkBuffer buffer) {
            final int limit = buffer.nioBuffer.limit();
            final int length = limit - buffer.readIndex();
            assert length >= 0 : "Invalid remaining: " + length;
            final byte[] bytes = new byte[length];
            buffer.nioBuffer.get(buffer.readIndex(), bytes);
            buffer.readIndex += length;
            return bytes;
        }
    }

    record StringType() implements NetworkBufferTypeImpl<String> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, String value) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            buffer.write(VAR_INT, bytes.length);
            buffer.write(RAW_BYTES, bytes);
        }

        @Override
        public String read(@NotNull NetworkBuffer buffer) {
            final int length = buffer.read(VAR_INT);
            final int remaining = buffer.nioBuffer.limit() - buffer.readIndex();
            Check.argCondition(length > remaining, "String is too long (length: {0}, readable: {1})", length, remaining);
            byte[] bytes = new byte[length];
            buffer.nioBuffer.get(buffer.readIndex(), bytes);
            buffer.readIndex += length;
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    record NbtType() implements NetworkBufferTypeImpl<BinaryTag> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, BinaryTag value) {
            BinaryTagWriter nbtWriter = buffer.nbtWriter;
            if (nbtWriter == null) {
                nbtWriter = new BinaryTagWriter(new DataOutputStream(new OutputStream() {
                    @Override
                    public void write(int b) {
                        buffer.write(BYTE, (byte) b);
                    }
                }));
                buffer.nbtWriter = nbtWriter;
            }
            try {
                nbtWriter.writeNameless(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public BinaryTag read(@NotNull NetworkBuffer buffer) {
            BinaryTagReader nbtReader = buffer.nbtReader;
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
                buffer.nbtReader = nbtReader;
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
            final byte[] bytes = new byte[length];
            buffer.nioBuffer.get(buffer.readIndex(), bytes);
            buffer.readIndex += length;
            return bytes;
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

    record DeathLocationType() implements NetworkBufferTypeImpl<WorldPos> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, WorldPos value) {
            buffer.writeOptional(value);
        }

        @Override
        public WorldPos read(@NotNull NetworkBuffer buffer) {
            return buffer.readOptional(WorldPos::new);
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

    record EnumType<E extends Enum<?>>(@NotNull Class<E> enumClass) implements NetworkBufferTypeImpl<E> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, E value) {
            buffer.writeEnum(enumClass, value);
        }

        @Override
        public E read(@NotNull NetworkBuffer buffer) {
            return buffer.readEnum(enumClass);
        }
    }

    record OptionalType<T>(@NotNull Type<T> parent) implements NetworkBufferTypeImpl<@Nullable T> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, T value) {
            buffer.writeOptional(parent, value);
        }

        @Override
        public T read(@NotNull NetworkBuffer buffer) {
            return buffer.readOptional(parent);
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

    record MappedType<T, S>(@NotNull Type<T> parent, @NotNull Function<T, S> to,
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

    record ListType<T>(@NotNull Type<T> parent, int maxSize) implements NetworkBufferTypeImpl<List<T>> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, List<T> value) {
            buffer.writeCollection(parent, value);
        }

        @Override
        public List<T> read(@NotNull NetworkBuffer buffer) {
            return buffer.readCollection(parent, maxSize);
        }
    }

    record OptionalTypeImpl<T>(@NotNull Type<T> parent) implements NetworkBufferTypeImpl<T> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, T value) {
            buffer.writeOptional(parent, value);
        }

        @Override
        public T read(@NotNull NetworkBuffer buffer) {
            return buffer.readOptional(parent);
        }
    }

    record RegistryTypeType<T extends ProtocolObject>(
            @NotNull Function<Registries, DynamicRegistry<T>> selector) implements NetworkBufferTypeImpl<DynamicRegistry.Key<T>> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, DynamicRegistry.Key<T> value) {
            Check.stateCondition(buffer.registries == null, "Buffer does not have registries");
            final DynamicRegistry<T> registry = selector.apply(buffer.registries);
            // Painting variants may be sent in their entirety rather than a registry reference so the ID is offset by 1 to indicate this.
            // FIXME: Support sending the entire registry object instead of an ID reference.
            final int id = registry.id().equals("minecraft:painting_variant")?registry.getId(value)+1:registry.getId(value);
            Check.argCondition(id == -1, "Key is not registered: {0} > {1}", registry, value);
            buffer.write(VAR_INT, id);
        }

        @Override
        public DynamicRegistry.Key<T> read(@NotNull NetworkBuffer buffer) {
            Check.stateCondition(buffer.registries == null, "Buffer does not have registries");
            DynamicRegistry<T> registry = selector.apply(buffer.registries);
            final int id = buffer.read(VAR_INT);
            final DynamicRegistry.Key<T> key = registry.getKey(id);
            Check.argCondition(key == null, "No such ID in registry: {0} > {1}", registry, id);
            return key;
        }
    }
}
