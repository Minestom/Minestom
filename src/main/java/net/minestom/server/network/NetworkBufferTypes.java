package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class NetworkBufferTypes {
    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    static final TypeImpl<Boolean> BOOLEAN = new TypeImpl<>(Boolean.class,
            (buffer, value) -> {
                buffer.ensureSize(1);
                buffer.nioBuffer.put(buffer.writeIndex(), value ? (byte) 1 : (byte) 0);
                return 1;
            },
            buffer -> {
                final byte value = buffer.nioBuffer.get(buffer.readIndex());
                buffer.readIndex += 1;
                return value == 1;
            });
    static final TypeImpl<Byte> BYTE = new TypeImpl<>(Byte.class,
            (buffer, value) -> {
                buffer.ensureSize(1);
                buffer.nioBuffer.put(buffer.writeIndex(), value);
                return 1;
            },
            buffer -> {
                final byte value = buffer.nioBuffer.get(buffer.readIndex());
                buffer.readIndex += 1;
                return value;
            });
    static final TypeImpl<Short> SHORT = new TypeImpl<>(Short.class,
            (buffer, value) -> {
                buffer.ensureSize(2);
                buffer.nioBuffer.putShort(buffer.writeIndex(), value);
                return 2;
            },
            buffer -> {
                final short value = buffer.nioBuffer.getShort(buffer.readIndex());
                buffer.readIndex += 2;
                return value;
            });
    static final TypeImpl<Integer> INT = new TypeImpl<>(Integer.class,
            (buffer, value) -> {
                buffer.ensureSize(4);
                buffer.nioBuffer.putInt(buffer.writeIndex(), value);
                return 4;
            },
            buffer -> {
                final int value = buffer.nioBuffer.getInt(buffer.readIndex());
                buffer.readIndex += 4;
                return value;
            });
    static final TypeImpl<Long> LONG = new TypeImpl<>(Long.class,
            (buffer, value) -> {
                buffer.ensureSize(8);
                buffer.nioBuffer.putLong(buffer.writeIndex(), value);
                return 8;
            },
            buffer -> {
                final long value = buffer.nioBuffer.getLong(buffer.readIndex());
                buffer.readIndex += 8;
                return value;
            });
    static final TypeImpl<Float> FLOAT = new TypeImpl<>(Float.class,
            (buffer, value) -> {
                buffer.ensureSize(4);
                buffer.nioBuffer.putFloat(buffer.writeIndex(), value);
                return 4;
            },
            buffer -> {
                final float value = buffer.nioBuffer.getFloat(buffer.readIndex());
                buffer.readIndex += 4;
                return value;
            });
    static final TypeImpl<Double> DOUBLE = new TypeImpl<>(Double.class,
            (buffer, value) -> {
                buffer.ensureSize(8);
                buffer.nioBuffer.putDouble(buffer.writeIndex(), value);
                return 8;
            },
            buffer -> {
                final double value = buffer.nioBuffer.getDouble(buffer.readIndex());
                buffer.readIndex += 8;
                return value;
            });
    static final TypeImpl<Integer> VAR_INT = new TypeImpl<>(Integer.class,
            (buffer, boxed) -> {
                final int value = boxed;
                final int index = buffer.writeIndex();
                var nio = buffer.nioBuffer;
                if ((value & (0xFFFFFFFF << 7)) == 0) {
                    buffer.ensureSize(1);
                    nio.put(index, (byte) value);
                    return 1;
                } else if ((value & (0xFFFFFFFF << 14)) == 0) {
                    buffer.ensureSize(2);
                    nio.putShort(index, (short) ((value & 0x7F | 0x80) << 8 | (value >>> 7)));
                    return 2;
                } else if ((value & (0xFFFFFFFF << 21)) == 0) {
                    buffer.ensureSize(3);
                    nio.put(index, (byte) (value & 0x7F | 0x80));
                    nio.put(index + 1, (byte) ((value >>> 7) & 0x7F | 0x80));
                    nio.put(index + 2, (byte) (value >>> 14));
                    return 3;
                } else if ((value & (0xFFFFFFFF << 28)) == 0) {
                    buffer.ensureSize(4);
                    nio.putInt(index, (value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                            | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21));
                    return 4;
                } else {
                    buffer.ensureSize(5);
                    nio.putInt(index, (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                            | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80));
                    nio.put(index + 4, (byte) (value >>> 28));
                    return 5;
                }
            },
            buffer -> {
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
            });
    static final TypeImpl<Long> VAR_LONG = new TypeImpl<>(Long.class,
            (buffer, value) -> {
                buffer.ensureSize(10);
                int size = 0;
                while (true) {
                    if ((value & ~((long) SEGMENT_BITS)) == 0) {
                        buffer.nioBuffer.put(buffer.writeIndex() + size, (byte) value.intValue());
                        return size + 1;
                    }
                    buffer.nioBuffer.put(buffer.writeIndex() + size, (byte) (value & SEGMENT_BITS | CONTINUE_BIT));
                    size++;
                    // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
                    value >>>= 7;
                }
            },
            buffer -> {
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
            });
    static final TypeImpl<byte[]> RAW_BYTES = new TypeImpl<>(byte[].class,
            (buffer, value) -> {
                buffer.ensureSize(value.length);
                buffer.nioBuffer.put(buffer.writeIndex(), value);
                return value.length;
            },
            buffer -> {
                final int length = buffer.readableBytes();
                assert length > 0 : "Invalid remaining: " + length;
                final byte[] bytes = new byte[length];
                buffer.nioBuffer.get(buffer.readIndex(), bytes);
                buffer.readIndex += length;
                return bytes;
            });
    static final TypeImpl<String> STRING = new TypeImpl<>(String.class,
            (buffer, value) -> {
                final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                buffer.write(VAR_INT, bytes.length);
                buffer.write(RAW_BYTES, bytes);
                return -1;
            },
            buffer -> {
                final int length = buffer.read(VAR_INT);
                byte[] bytes = new byte[length];
                buffer.nioBuffer.get(buffer.readIndex(), bytes);
                buffer.readIndex += length;
                return new String(bytes, StandardCharsets.UTF_8);
            });
    static final TypeImpl<NBT> NBT = new TypeImpl<>(NBT.class,
            (buffer, value) -> {
                NBTWriter nbtWriter = buffer.nbtWriter;
                if (nbtWriter == null) {
                    nbtWriter = new NBTWriter(new OutputStream() {
                        @Override
                        public void write(int b) {
                            buffer.write(BYTE, (byte) b);
                        }
                    }, CompressedProcesser.NONE);
                    buffer.nbtWriter = nbtWriter;
                }
                try {
                    nbtWriter.writeNamed("", value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return -1;
            },
            buffer -> {
                NBTReader nbtReader = buffer.nbtReader;
                if (nbtReader == null) {
                    nbtReader = new NBTReader(new InputStream() {
                        @Override
                        public int read() {
                            return buffer.read(BYTE);
                        }
                    }, CompressedProcesser.NONE);
                    buffer.nbtReader = nbtReader;
                }
                try {
                    return nbtReader.read();
                } catch (IOException | NBTException e) {
                    throw new RuntimeException(e);
                }
            });
    static final TypeImpl<Point> BLOCK_POSITION = new TypeImpl<>(Point.class,
            (buffer, value) -> {
                final int blockX = value.blockX();
                final int blockY = value.blockY();
                final int blockZ = value.blockZ();
                final long longPos = (((long) blockX & 0x3FFFFFF) << 38) |
                        (((long) blockZ & 0x3FFFFFF) << 12) |
                        ((long) blockY & 0xFFF);
                buffer.write(LONG, longPos);
                return -1;
            },
            buffer -> {
                final long value = buffer.read(LONG);
                final int x = (int) (value >> 38);
                final int y = (int) (value << 52 >> 52);
                final int z = (int) (value << 26 >> 38);
                return new Vec(x, y, z);
            });
    static final TypeImpl<Component> COMPONENT = new TypeImpl<>(Component.class,
            (buffer, value) -> {
                final String json = GsonComponentSerializer.gson().serialize(value);
                buffer.write(STRING, json);
                return -1;
            },
            buffer -> {
                final String json = buffer.read(STRING);
                return GsonComponentSerializer.gson().deserialize(json);
            });
    static final TypeImpl<UUID> UUID = new TypeImpl<>(UUID.class,
            (buffer, value) -> {
                buffer.write(LONG, value.getMostSignificantBits());
                buffer.write(LONG, value.getLeastSignificantBits());
                return -1;
            },
            buffer -> {
                final long mostSignificantBits = buffer.read(LONG);
                final long leastSignificantBits = buffer.read(LONG);
                return new UUID(mostSignificantBits, leastSignificantBits);
            });
    static final TypeImpl<ItemStack> ITEM = new TypeImpl<>(ItemStack.class,
            (buffer, value) -> {
                if (value.isAir()) {
                    buffer.write(BOOLEAN, false);
                    return -1;
                }
                buffer.write(BOOLEAN, true);
                buffer.write(VAR_INT, value.material().id());
                buffer.write(BYTE, (byte) value.amount());
                buffer.write(NBT, value.meta().toNBT());
                return -1;
            },
            buffer -> {
                final boolean present = buffer.read(BOOLEAN);
                if (!present) return ItemStack.AIR;

                final int id = buffer.read(VAR_INT);
                final Material material = Material.fromId(id);
                if (material == null) throw new RuntimeException("Unknown material id: " + id);

                final int amount = buffer.read(BYTE);
                final NBT nbt = buffer.read(NBT);
                if (!(nbt instanceof NBTCompound compound)) {
                    return ItemStack.of(material, amount);
                }

                return ItemStack.fromNBT(material, compound, amount);
            });

    record TypeImpl<T>(@NotNull Class<T> type,
                       @NotNull TypeWriter<T> writer,
                       @NotNull TypeReader<T> reader) implements NetworkBuffer.Type<T> {
    }

    interface TypeWriter<T> {
        long write(@NotNull NetworkBuffer buffer, @NotNull T value);
    }

    interface TypeReader<T> {
        @NotNull T read(@NotNull NetworkBuffer buffer);
    }
}
