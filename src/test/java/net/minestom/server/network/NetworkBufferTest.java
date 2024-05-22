package net.minestom.server.network;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.kyori.adventure.nbt.IntBinaryTag.intBinaryTag;
import static net.minestom.server.network.NetworkBuffer.*;
import static org.junit.jupiter.api.Assertions.*;

class NetworkBufferTest {

    @Test
    void resize() {
        var buffer = new NetworkBuffer(6);
        buffer.write(INT, 6);
        assertEquals(4, buffer.writeIndex());

        buffer.write(INT, 7);
        assertEquals(8, buffer.writeIndex());

        assertEquals(6, buffer.read(INT));
        assertEquals(7, buffer.read(INT));

        // Test one-off length
        buffer = new NetworkBuffer(1);
        buffer.write(BYTE, (byte) 3);
        assertEquals(1, buffer.writeIndex());

        buffer.write(BYTE, (byte) 4);
        assertEquals(2, buffer.writeIndex());

        assertEquals((byte) 3, buffer.read(BYTE));
        assertEquals((byte) 4, buffer.read(BYTE));
    }

    @Test
    void readableBytes() {
        var buffer = new NetworkBuffer();
        assertEquals(0, buffer.readableBytes());

        buffer.write(BYTE, (byte) 0);
        assertEquals(1, buffer.readableBytes());

        buffer.write(LONG, 50L);
        assertEquals(9, buffer.readableBytes());

        assertEquals((byte) 0, buffer.read(BYTE));
        assertEquals(8, buffer.readableBytes());

        assertEquals(50L, buffer.read(LONG));
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void extractBytes() {
        var buffer = new NetworkBuffer();

        buffer.write(BYTE, (byte) 25);
        assertEquals(1, buffer.writeIndex());
        assertEquals(0, buffer.readIndex());

        var array = buffer.extractBytes(extractor -> extractor.read(BYTE));
        assertArrayEquals(new byte[]{25}, array, "Unequal array: " + Arrays.toString(array));
        assertEquals(1, buffer.writeIndex());
        assertEquals(1, buffer.readIndex());

        buffer.write(BYTE, (byte) 25);
        buffer.write(LONG, 50L);
        assertEquals(10, buffer.writeIndex());
        assertEquals(1, buffer.readIndex());

        array = buffer.extractBytes(extractor -> {
            extractor.read(BYTE);
            extractor.read(LONG);
        });
        assertArrayEquals(new byte[]{25, 0, 0, 0, 0, 0, 0, 0, 50}, array, "Unequal array: " + Arrays.toString(array));
        assertEquals(10, buffer.writeIndex());
        assertEquals(10, buffer.readIndex());
    }

    @Test
    void makeArray() {
        assertArrayEquals(new byte[0], NetworkBuffer.makeArray(buffer -> {
        }));

        assertArrayEquals(new byte[]{1}, NetworkBuffer.makeArray(buffer -> buffer.write(BYTE, (byte) 1)));

        assertArrayEquals(new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 50}, NetworkBuffer.makeArray(buffer -> {
            buffer.write(BYTE, (byte) 1);
            buffer.write(LONG, 50L);
        }));
    }

    @Test
    void numbers() {
        assertBufferType(BOOLEAN, false, new byte[]{0x00});
        assertBufferType(BOOLEAN, true, new byte[]{0x01});

        assertBufferType(BYTE, (byte) 0x00, new byte[]{0x00});
        assertBufferType(BYTE, (byte) 0x01, new byte[]{0x01});
        assertBufferType(BYTE, (byte) 0x7F, new byte[]{0x7F});
        assertBufferType(BYTE, (byte) 0x80, new byte[]{(byte) 0x80});
        assertBufferType(BYTE, (byte) 0xFF, new byte[]{(byte) 0xFF});

        assertBufferType(SHORT, (short) 0x0000, new byte[]{0x00, 0x00});
        assertBufferType(SHORT, (short) 0x0001, new byte[]{0x00, 0x01});
        assertBufferType(SHORT, (short) 0x7FFF, new byte[]{0x7F, (byte) 0xFF});
        assertBufferType(SHORT, (short) 0x8000, new byte[]{(byte) 0x80, 0x00});
        assertBufferType(SHORT, (short) 0xFFFF, new byte[]{(byte) 0xFF, (byte) 0xFF});

        assertBufferType(UNSIGNED_SHORT, 0x0000, new byte[]{0x00, 0x00});
        assertBufferType(UNSIGNED_SHORT, 0x0001, new byte[]{0x00, 0x01});
        assertBufferType(UNSIGNED_SHORT, 0x7FFF, new byte[]{0x7F, (byte) 0xFF});
        assertBufferType(UNSIGNED_SHORT, 0x8000, new byte[]{(byte) 0x80, 0x00});
        assertBufferType(UNSIGNED_SHORT, 0xFFFF, new byte[]{(byte) 0xFF, (byte) 0xFF});

        assertBufferType(INT, 0, new byte[]{0x00, 0x00, 0x00, 0x00});
        assertBufferType(INT, 1, new byte[]{0x00, 0x00, 0x00, 0x01});
        assertBufferType(INT, 2, new byte[]{0x00, 0x00, 0x00, 0x02});
        assertBufferType(INT, 127, new byte[]{0x00, 0x00, 0x00, 0x7F});
        assertBufferType(INT, 128, new byte[]{0x00, 0x00, 0x00, (byte) 0x80});
        assertBufferType(INT, 255, new byte[]{0x00, 0x00, 0x00, (byte) 0xFF});
        assertBufferType(INT, 256, new byte[]{0x00, 0x00, 0x01, 0x00});
        assertBufferType(INT, 25565, new byte[]{0x00, 0x00, 0x63, (byte) 0xDD});
        assertBufferType(INT, 32767, new byte[]{0x00, 0x00, 0x7F, (byte) 0xFF});
        assertBufferType(INT, 32768, new byte[]{0x00, 0x00, (byte) 0x80, 0x00});
        assertBufferType(INT, 65535, new byte[]{0x00, 0x00, (byte) 0xFF, (byte) 0xFF});
        assertBufferType(INT, 65536, new byte[]{0x00, 0x01, 0x00, 0x00});
        assertBufferType(INT, 2147483647, new byte[]{0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        assertBufferType(INT, -1, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        assertBufferType(INT, -2147483648, new byte[]{(byte) 0x80, 0x00, 0x00, 0x00});

        assertBufferType(LONG, 0L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(LONG, 1L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01});
        assertBufferType(LONG, 2L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02});
        assertBufferType(LONG, 127L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7F});
        assertBufferType(LONG, 128L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80});
        assertBufferType(LONG, 255L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF});
        assertBufferType(LONG, 256L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00});
        assertBufferType(LONG, 25565L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x63, (byte) 0xDD});
        assertBufferType(LONG, 32767L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7F, (byte) 0xFF});
        assertBufferType(LONG, 32768L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00});
        assertBufferType(LONG, 65535L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF});
        assertBufferType(LONG, 65536L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00});
        assertBufferType(LONG, 2147483647L, new byte[]{0x00, 0x00, 0x00, 0x00, 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        assertBufferType(LONG, 2147483648L, new byte[]{0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00, 0x00, 0x00});
        assertBufferType(LONG, 4294967295L, new byte[]{0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        assertBufferType(LONG, 4294967296L, new byte[]{0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(LONG, 9223372036854775807L, new byte[]{0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        assertBufferType(LONG, -1L, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        assertBufferType(LONG, -2147483648L, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x80, 0x00, 0x00, 0x00});
        assertBufferType(LONG, -4294967296L, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(LONG, -9223372036854775808L, new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

        assertBufferType(FLOAT, 0f, new byte[]{0x00, 0x00, 0x00, 0x00});
        assertBufferType(FLOAT, 1f, new byte[]{0x3F, (byte) 0x80, 0x00, 0x00});
        assertBufferType(FLOAT, 1.1f, new byte[]{0x3F, (byte) 0x8C, (byte) 0xCC, (byte) 0xCD});
        assertBufferType(FLOAT, 1.5f, new byte[]{0x3F, (byte) 0xC0, 0x00, 0x00});
        assertBufferType(FLOAT, 1.6f, new byte[]{0x3F, (byte) 0xCC, (byte) 0xCC, (byte) 0xCD});
        assertBufferType(FLOAT, 2f, new byte[]{0x40, 0x00, 0x00, 0x00});
        assertBufferType(FLOAT, 2.5f, new byte[]{0x40, 0x20, 0x00, 0x00});
        assertBufferType(FLOAT, 3f, new byte[]{0x40, 0x40, 0x00, 0x00});
        assertBufferType(FLOAT, 4f, new byte[]{0x40, (byte) 0x80, 0x00, 0x00});
        assertBufferType(FLOAT, 5f, new byte[]{0x40, (byte) 0xA0, 0x00, 0x00});
        assertBufferType(FLOAT, 10f, new byte[]{0x41, 0x20, 0x00, 0x00});
        assertBufferType(FLOAT, 100f, new byte[]{0x42, (byte) 0xC8, 0x00, 0x00});
        assertBufferType(FLOAT, 1000f, new byte[]{0x44, 0x7a, 0x00, 0x00});
        assertBufferType(FLOAT, 10000f, new byte[]{0x46, 0x1C, 0x40, 0x00});
        assertBufferType(FLOAT, 100000f, new byte[]{0x47, (byte) 0xC3, 0x50, 0x00});

        assertBufferType(DOUBLE, 0d, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 1d, new byte[]{0x3F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 1.1d, new byte[]{0x3F, (byte) 0xF1, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x9A});
        assertBufferType(DOUBLE, 1.5d, new byte[]{0x3F, (byte) 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 1.6d, new byte[]{0x3F, (byte) 0xF9, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x9A});
        assertBufferType(DOUBLE, 2d, new byte[]{0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 2.5d, new byte[]{0x40, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 3d, new byte[]{0x40, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 4d, new byte[]{0x40, 0x10, (byte) 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 5d, new byte[]{0x40, 0x14, (byte) 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 10d, new byte[]{0x40, 0x24, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 100d, new byte[]{0x40, 0x59, (byte) 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 1000d, new byte[]{0x40, (byte) 0x8F, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertBufferType(DOUBLE, 10000d, new byte[]{0x40, (byte) 0xC3, (byte) 0x88, 0x00, 0x00, 0x00, 0x00, 0x00});
    }

    @Test
    void varInt() {
        assertBufferType(VAR_INT, 0, new byte[]{0});
        assertBufferType(VAR_INT, 1, new byte[]{0x01});
        assertBufferType(VAR_INT, 2, new byte[]{0x02});
        assertBufferType(VAR_INT, 11, new byte[]{0x0B});
        assertBufferType(VAR_INT, 127, new byte[]{0x7f});
        assertBufferType(VAR_INT, 128, new byte[]{(byte) 0x80, 0x01});
        assertBufferType(VAR_INT, 255, new byte[]{(byte) 0xff, 0x01});
        assertBufferType(VAR_INT, 25565, new byte[]{(byte) 0xdd, (byte) 0xc7, 0x01});
        assertBufferType(VAR_INT, 2097151, new byte[]{(byte) 0xff, (byte) 0xff, 0x7f});
        assertBufferType(VAR_INT, 2147483647, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x07});
        assertBufferType(VAR_INT, -1, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x0f});
        assertBufferType(VAR_INT, -2147483648, new byte[]{(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x08});
    }

    @Test
    void varLong() {
        assertBufferType(VAR_LONG, 0L, new byte[]{0});
        assertBufferType(VAR_LONG, 1L, new byte[]{0x01});
        assertBufferType(VAR_LONG, 2L, new byte[]{0x02});
        assertBufferType(VAR_LONG, 127L, new byte[]{0x7f});
        assertBufferType(VAR_LONG, 128L, new byte[]{(byte) 0x80, 0x01});
        assertBufferType(VAR_LONG, 255L, new byte[]{(byte) 0xff, 0x01});
        assertBufferType(VAR_LONG, 2147483647L, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x07});
        assertBufferType(VAR_LONG, 9223372036854775807L, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x7f});
        assertBufferType(VAR_LONG, -1L, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x01});
        assertBufferType(VAR_LONG, -2147483648L, new byte[]{(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xf8, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x01});
        assertBufferType(VAR_LONG, -9223372036854775808L, new byte[]{(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x01});
    }

    @Test
    void rawBytes() {
        // FIXME: currently break because the array is identity compared
        //assertBufferType(NetworkBuffer.RAW_BYTES, new byte[]{0x0B, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64},
        //      new byte[]{0x0B, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64});
    }

    @Test
    void string() {
        assertBufferType(STRING, "Hello World", new byte[]{0x0B, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64});
    }

    @Test
    public void nbt() {
        assertBufferType(NetworkBuffer.NBT, intBinaryTag(5));
        assertBufferType(NetworkBuffer.NBT, CompoundBinaryTag.from(Map.of("key", intBinaryTag(5))));
    }

    @Test
    void component() {
        assertBufferType(COMPONENT, Component.text("Hello world"));
    }

    @Test
    void uuid() {
        assertBufferType(UUID, new UUID(0, 0), new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        assertBufferType(UUID, new UUID(1, 1), new byte[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1});
    }

    @Test
    public void item() {
        assertBufferType(ItemStack.NETWORK_TYPE, ItemStack.AIR);
        assertBufferType(ItemStack.NETWORK_TYPE, ItemStack.of(Material.STONE, 1));
        assertBufferType(ItemStack.NETWORK_TYPE, ItemStack.of(Material.DIAMOND_AXE, 1).with(ItemComponent.DAMAGE, 1));
    }

    @Test
    void optional() {
        assertBufferTypeOptional(BOOLEAN, null, new byte[]{0});
        assertBufferTypeOptional(BOOLEAN, true, new byte[]{1, 1});
    }

    @Test
    void collection() {
        assertBufferTypeCollection(BOOLEAN, List.of(), new byte[]{0});
        assertBufferTypeCollection(BOOLEAN, List.of(true), new byte[]{0x01, 0x01});
    }

    @Test
    public void collectionMaxSize() {
        var buffer = new NetworkBuffer();
        var list = new ArrayList<Boolean>();
        for (int i = 0; i < 1000; i++)
            list.add(true);
        buffer.writeCollection(BOOLEAN, list);

        assertThrows(IllegalArgumentException.class, () -> buffer.readCollection(BOOLEAN, 10));
        buffer.readIndex(0); // reset
        assertThrows(IllegalArgumentException.class, () -> buffer.readCollection(b -> b.read(BOOLEAN), 10));
    }

    @Test
    public void oomStringRegression() {
        var buffer = new NetworkBuffer(ByteBuffer.allocate(100));
        buffer.write(VAR_INT, Integer.MAX_VALUE); // String length
        buffer.write(RAW_BYTES, "Hello".getBytes(StandardCharsets.UTF_8)); // String data

        assertThrows(IllegalArgumentException.class, () -> buffer.read(STRING)); // oom
    }

    static <T> void assertBufferType(NetworkBuffer.@NotNull Type<T> type, @UnknownNullability T value, byte[] expected, @NotNull Action<T> action) {
        var buffer = new NetworkBuffer();
        action.write(buffer, type, value);
        assertEquals(0, buffer.readIndex());
        if (expected != null) assertEquals(expected.length, buffer.writeIndex());

        var actual = action.read(buffer, type);

        assertEquals(value, actual);
        if (expected != null) assertEquals(expected.length, buffer.readIndex(), "Invalid read index");
        if (expected != null) assertEquals(expected.length, buffer.writeIndex());

        if (expected != null) {
            var bytes = new byte[expected.length];
            buffer.copyTo(0, bytes, 0, bytes.length);
            assertArrayEquals(expected, bytes, "Invalid bytes: " + Arrays.toString(expected) + " != " + Arrays.toString(bytes));
        }

        // Ensure resize support
        {
            var tmp = new NetworkBuffer(0);
            action.write(tmp, type, value);
            assertEquals(0, tmp.readIndex());
            if (expected != null) assertEquals(expected.length, tmp.writeIndex());

            var tmpRead = action.read(tmp, type);

            assertEquals(value, tmpRead);
            if (expected != null) assertEquals(expected.length, tmp.readIndex(), "Invalid read index");
            if (expected != null) assertEquals(expected.length, tmp.writeIndex());
        }
    }

    static <T> void assertBufferType(NetworkBuffer.@NotNull Type<T> type, @NotNull T value, byte @Nullable [] expected) {
        assertBufferType(type, value, expected, new Action<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, @NotNull NetworkBuffer.Type<T> type, @UnknownNullability T value) {
                buffer.write(type, value);
            }

            @Override
            public T read(@NotNull NetworkBuffer buffer, @NotNull NetworkBuffer.Type<T> type) {
                return buffer.read(type);
            }
        });
    }

    static <T> void assertBufferType(NetworkBuffer.@NotNull Type<T> type, @NotNull T value) {
        assertBufferType(type, value, null);
    }

    static <T> void assertBufferTypeOptional(NetworkBuffer.@NotNull Type<T> type, @Nullable T value, byte @Nullable [] expected) {
        assertBufferType(type, value, expected, new Action<T>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, @NotNull NetworkBuffer.Type<T> type, @UnknownNullability T value) {
                buffer.writeOptional(type, value);
            }

            @Override
            public T read(@NotNull NetworkBuffer buffer, @NotNull NetworkBuffer.Type<T> type) {
                return buffer.readOptional(type);
            }
        });
    }

    static <T> void assertBufferTypeOptional(NetworkBuffer.@NotNull Type<T> type, @Nullable T value) {
        assertBufferTypeOptional(type, value, null);
    }

    static <T> void assertBufferTypeCollection(NetworkBuffer.@NotNull Type<T> type, @NotNull Collection<T> values, byte @Nullable [] expected) {
        var buffer = new NetworkBuffer();
        buffer.writeCollection(type, values);
        assertEquals(0, buffer.readIndex());
        if (expected != null) assertEquals(expected.length, buffer.writeIndex());

        var actual = buffer.readCollection(type, Integer.MAX_VALUE);

        assertEquals(values, actual);
        if (expected != null) assertEquals(expected.length, buffer.readIndex());
        if (expected != null) assertEquals(expected.length, buffer.writeIndex());

        if (expected != null) {
            var bytes = new byte[expected.length];
            buffer.copyTo(0, bytes, 0, bytes.length);
            assertArrayEquals(expected, bytes, "Invalid bytes: " + Arrays.toString(expected) + " != " + Arrays.toString(bytes));
        }
    }

    static <T> void assertBufferTypeCollection(NetworkBuffer.@NotNull Type<T> type, @NotNull Collection<T> value) {
        assertBufferTypeCollection(type, value, null);
    }

    interface Action<T> {
        void write(@NotNull NetworkBuffer buffer, NetworkBuffer.@NotNull Type<T> type, @UnknownNullability T value);

        T read(@NotNull NetworkBuffer buffer, NetworkBuffer.@NotNull Type<T> type);
    }
}
