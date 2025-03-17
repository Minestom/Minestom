package net.minestom.server.item;

import net.kyori.adventure.nbt.*;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

final class ItemStackHashImpl {

    public static final NetworkBuffer.Type<ItemStack.Hash> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ItemStack.Hash value) {
            if (!(value instanceof Item item)) {
                buffer.write(NetworkBuffer.BOOLEAN, false);
                return;
            }

            buffer.write(NetworkBuffer.BOOLEAN, true);
            buffer.write(Item.NETWORK_TYPE, item);
        }

        @Override
        public ItemStack.Hash read(@NotNull NetworkBuffer buffer) {
            if (!buffer.read(NetworkBuffer.BOOLEAN))
                return ItemStack.Hash.AIR;
            return buffer.read(Item.NETWORK_TYPE);
        }
    };

    record Air() implements ItemStack.Hash {
    }

    record Item(
            @NotNull Material material,
            int amount,
            @NotNull Map<DataComponent<?>, Integer> addedComponents,
            @NotNull Set<DataComponent<?>> removedComponents
    ) implements ItemStack.Hash {
        private static final int MAX_COMPONENTS = 256;
        public static final NetworkBuffer.Type<Item> NETWORK_TYPE = NetworkBufferTemplate.template(
                Material.NETWORK_TYPE, Item::material,
                NetworkBuffer.VAR_INT, Item::amount,
                DataComponent.NETWORK_TYPE.mapValue(NetworkBuffer.INT, MAX_COMPONENTS), Item::addedComponents,
                DataComponent.NETWORK_TYPE.set(MAX_COMPONENTS), Item::removedComponents,
                Item::new);
    }

    // Hashing implementation below:

    private static final byte TAG_EMPTY = 1;
    private static final byte TAG_MAP_START = 2;
    private static final byte TAG_MAP_END = 3;
    private static final byte TAG_LIST_START = 4;
    private static final byte TAG_LIST_END = 5;
    private static final byte TAG_BYTE = 6;
    private static final byte TAG_SHORT = 7;
    private static final byte TAG_INT = 8;
    private static final byte TAG_LONG = 9;
    private static final byte TAG_FLOAT = 10;
    private static final byte TAG_DOUBLE = 11;
    private static final byte TAG_STRING = 12;
    private static final byte TAG_BOOLEAN = 13;
    private static final byte TAG_BYTE_ARRAY_START = 14;
    private static final byte TAG_BYTE_ARRAY_END = 15;
    private static final byte TAG_INT_ARRAY_START = 16;
    private static final byte TAG_INT_ARRAY_END = 17;
    private static final byte TAG_LONG_ARRAY_START = 18;
    private static final byte TAG_LONG_ARRAY_END = 19;

    private static final int EMPTY = new Hasher().putByte(TAG_EMPTY).hash();
    private static final int EMPTY_MAP = new Hasher().putByte(TAG_MAP_START).putByte(TAG_MAP_END).hash();
    private static final int EMPTY_LIST = new Hasher().putByte(TAG_LIST_START).putByte(TAG_LIST_END).hash();
    private static final int FALSE = new Hasher().putByte(TAG_BOOLEAN).putByte((byte) 0).hash();
    private static final int TRUE = new Hasher().putByte(TAG_BOOLEAN).putByte((byte) 1).hash();

    private static int hashBinaryTag(@NotNull BinaryTag binaryTag) {
        return switch (binaryTag) {
            case EndBinaryTag tag -> EMPTY;
            case ByteBinaryTag tag -> {
                //TODO(1.21.5) this is wrong, add a comment.
                if (tag.value() == 0) yield FALSE;
                if (tag.value() == 1) yield TRUE;
                yield new Hasher().putByte(TAG_BYTE).putByte(tag.value()).hash();
            }
            case ShortBinaryTag tag -> new Hasher().putByte(TAG_SHORT).putShort(tag.value()).hash();
            case IntBinaryTag tag -> new Hasher().putByte(TAG_INT).putInt(tag.value()).hash();
            case LongBinaryTag tag -> new Hasher().putByte(TAG_LONG).putLong(tag.value()).hash();
            case FloatBinaryTag tag -> new Hasher().putFloat(TAG_FLOAT).putFloat(tag.value()).hash();
            case DoubleBinaryTag tag -> new Hasher().putDouble(TAG_DOUBLE).putDouble(tag.value()).hash();
            case ByteArrayBinaryTag tag -> new Hasher().putByte(TAG_BYTE_ARRAY_START)
                    .putBytes(tag.value()).putByte(TAG_BYTE_ARRAY_END).hash();
            case StringBinaryTag tag -> new Hasher().putByte(TAG_STRING)
                    .putInt(tag.value().length())
                    .putBytes(tag.value().getBytes(StandardCharsets.UTF_8)).hash();
            case ListBinaryTag tag -> 0;
            case CompoundBinaryTag tag -> 0;
            case IntArrayBinaryTag tag -> 0;
            case LongArrayBinaryTag tag -> 0;
            default -> throw new IllegalArgumentException("Unknown tag type: " + binaryTag);
        };
    }

    // Loosely based on the Hasher implementation from Guava, licensed under the Apache 2.0 license.
    private record Hasher(@NotNull CRC32 crc32, @NotNull ByteBuffer buffer) {
        public Hasher() {
            this(new CRC32(), ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN));
        }

        private @NotNull Hasher update(int bytes) {
            buffer.limit(bytes);
            crc32.update(buffer);
            buffer.position(0);
            buffer.limit(8);
            return this;
        }

        public @NotNull Hasher putByte(byte b) {
            crc32.update(new byte[]{b});
            return this;
        }

        public @NotNull Hasher putShort(short s) {
            buffer.putShort(s);
            return update(Short.BYTES);
        }

        public @NotNull Hasher putInt(int i) {
            buffer.putInt(i);
            return update(Integer.BYTES);
        }

        public @NotNull Hasher putLong(long l) {
            buffer.putLong(l);
            return update(Long.BYTES);
        }

        public @NotNull Hasher putFloat(float f) {
            buffer.putFloat(f);
            return update(Float.BYTES);
        }

        public @NotNull Hasher putDouble(double d) {
            buffer.putDouble(d);
            return update(Double.BYTES);
        }

        public @NotNull Hasher putBytes(byte[] bytes) {
            crc32.update(bytes);
            return this;
        }

        public int hash() {
            return (int) crc32.getValue();
        }
    }

}
