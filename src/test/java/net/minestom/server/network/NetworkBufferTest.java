package net.minestom.server.network;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.kyori.adventure.nbt.IntBinaryTag.intBinaryTag;
import static net.minestom.server.network.NetworkBuffer.*;
import static org.junit.jupiter.api.Assertions.*;

public class NetworkBufferTest {

    @Test
    public void resize() {
        var buffer = NetworkBuffer.resizableBuffer(6);
        buffer.write(INT, 6);
        assertEquals(4, buffer.writeIndex());

        buffer.write(INT, 7);
        assertEquals(8, buffer.writeIndex());

        assertEquals(6, buffer.read(INT));
        assertEquals(7, buffer.read(INT));

        // Test one-off length
        buffer = NetworkBuffer.resizableBuffer(1);
        buffer.write(BYTE, (byte) 3);
        assertEquals(1, buffer.writeIndex());

        buffer.write(BYTE, (byte) 4);
        assertEquals(2, buffer.writeIndex());

        assertEquals((byte) 3, buffer.read(BYTE));
        assertEquals((byte) 4, buffer.read(BYTE));
    }

    @Test
    public void resizeRead() {
        var buffer = NetworkBuffer.resizableBuffer(4);
        buffer.write(INT, 6);
        assertEquals(4, buffer.capacity());
        assertEquals(4, buffer.writeIndex());

        buffer.resize(8);
        assertEquals(8, buffer.capacity());
        assertEquals(6, buffer.read(INT));

        buffer.write(INT, 7);
        assertEquals(8, buffer.capacity());
        assertEquals(8, buffer.writeIndex());
    }

    @Test
    public void copyClone() {
        var buffer = NetworkBuffer.staticBuffer(10);
        buffer.write(INT, 6);
        buffer.write(SHORT, (short) 2);
        buffer.write(FLOAT, 3.5f);
        assertEquals(10, buffer.writeIndex());
        assertEquals(10, buffer.capacity());

        var copy = buffer.copy(0, 10);
        assertEquals(10, copy.writeIndex());
        assertEquals(10, copy.capacity());

        assertTrue(NetworkBuffer.equals(buffer, copy));
    }

    @Test
    public void copyDirectZeroIndex() {
        var buffer1 = NetworkBuffer.staticBuffer(10);
        buffer1.write(INT, 6);
        buffer1.write(SHORT, (short) 2);
        buffer1.write(FLOAT, 3.5f);
        assertEquals(10, buffer1.writeIndex());
        assertEquals(10, buffer1.capacity());

        var buffer2 = NetworkBuffer.staticBuffer(10);
        NetworkBuffer.copy(buffer1, 0, buffer2, 0, 10);
        assertEquals(10, buffer2.capacity());

        assertEquals(6, buffer2.read(INT));
        assertEquals((short) 2, buffer2.read(SHORT));
        assertEquals(3.5f, buffer2.read(FLOAT));
    }

    @Test
    public void copyDirectIndex() {
        var buffer1 = NetworkBuffer.staticBuffer(10);
        buffer1.write(INT, 6);
        buffer1.write(SHORT, (short) 2);
        buffer1.write(FLOAT, 3.5f);
        assertEquals(10, buffer1.writeIndex());
        assertEquals(10, buffer1.capacity());

        var buffer2 = NetworkBuffer.staticBuffer(4);
        NetworkBuffer.copy(buffer1, 6, buffer2, 0, 4);
        assertEquals(4, buffer2.capacity());

        assertEquals(3.5f, buffer2.read(FLOAT));
    }

    @Test
    public void copyDirectIndexOffset() {
        var buffer1 = NetworkBuffer.staticBuffer(10);
        buffer1.write(INT, 6);
        buffer1.write(SHORT, (short) 2);
        buffer1.write(FLOAT, 3.5f);
        assertEquals(10, buffer1.writeIndex());
        assertEquals(10, buffer1.capacity());

        var buffer2 = NetworkBuffer.staticBuffer(8);
        buffer2.write(INT, 5);
        NetworkBuffer.copy(buffer1, 6, buffer2, 4, 4);
        assertEquals(8, buffer2.capacity());

        assertEquals(5, buffer2.read(INT));
        assertEquals(3.5f, buffer2.read(FLOAT));
    }

    @Test
    public void compact() {
        var buffer = NetworkBuffer.staticBuffer(256);
        buffer.write(INT, 6);
        buffer.write(SHORT, (short) 2);
        buffer.write(FLOAT, 3.5f);

        buffer.read(INT);
        buffer.compact();
        // Short should be copied at index 0
        assertEquals(256, buffer.capacity());
        assertEquals(6, buffer.writeIndex());
        assertEquals(0, buffer.readIndex());

        assertEquals((short) 2, buffer.read(SHORT));
        assertEquals(3.5f, buffer.read(FLOAT));
    }

    @Test
    public void outOfBound() {
        var buffer = NetworkBuffer.staticBuffer(3);
        buffer.write(SHORT, (short) 2);
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.write(INT, 6));
    }

    @Test
    public void readableBytes() {
        var buffer = NetworkBuffer.resizableBuffer();
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
    public void extractBytes() {
        var buffer = NetworkBuffer.resizableBuffer();

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
    public void makeArray() {
        assertArrayEquals(new byte[0], NetworkBuffer.makeArray(buffer -> {
        }));

        assertArrayEquals(new byte[]{1}, NetworkBuffer.makeArray(BYTE, (byte) 1));

        assertArrayEquals(new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 50}, NetworkBuffer.makeArray(buffer -> {
            buffer.write(BYTE, (byte) 1);
            buffer.write(LONG, 50L);
        }));
    }

    @Test
    public void arrayWrap() {
        byte[] array = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 50};
        var buffer = NetworkBuffer.wrap(array, 0, array.length);
        assertEquals(9, buffer.capacity());
        assertEquals(0, buffer.readIndex());
        assertEquals(array.length, buffer.writeIndex());

        assertEquals((byte) 1, buffer.read(BYTE));
        assertEquals(50L, buffer.read(LONG));

        assertEquals(9, buffer.readIndex());
    }

    @Test
    public void sizeOfPrimitives() {
        assertEquals(1, BYTE.sizeOf((byte) 1));
        assertEquals(2, SHORT.sizeOf((short) 1));
        assertEquals(4, INT.sizeOf(1));
        assertEquals(8, LONG.sizeOf(1L));
        assertEquals(4, FLOAT.sizeOf(1f));
        assertEquals(8, DOUBLE.sizeOf(1d));
    }

    @Test
    public void sizeOfCompounds() {
        var type = new Type<Integer>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Integer value) {
                buffer.write(INT, value);
                buffer.write(INT, value);
            }

            @Override
            public Integer read(@NotNull NetworkBuffer buffer) {
                throw new UnsupportedOperationException();
            }
        };

        assertEquals(8, type.sizeOf(1));
    }

    @Test
    public void sizeOfThrow() {
        Function<Consumer<NetworkBuffer>, Type<Integer>> fn = networkBufferConsumer -> new Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Integer value) {
                networkBufferConsumer.accept(buffer);
            }

            @Override
            public Integer read(@NotNull NetworkBuffer buffer) {
                throw new UnsupportedOperationException();
            }
        };

        assertThrows(UnsupportedOperationException.class, () -> fn.apply(buffer -> buffer.resize(2)).sizeOf(1));
        assertThrows(UnsupportedOperationException.class, () -> fn.apply(buffer -> buffer.read(INT)).sizeOf(1));
        assertThrows(UnsupportedOperationException.class, () -> fn.apply(buffer -> buffer.readAt(0, INT)).sizeOf(1));
        assertThrows(UnsupportedOperationException.class, () -> fn.apply(NetworkBuffer::compact).sizeOf(1));
        assertThrows(UnsupportedOperationException.class, () -> fn.apply(buffer -> buffer.slice(0, 0, 0, 0)).sizeOf(1));
        assertThrows(UnsupportedOperationException.class, () -> fn.apply(buffer -> buffer.copy(0, 0, 0, 0)).sizeOf(1));
    }

    @Test
    public void numbers() {
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
    public void varInt() {
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
    public void varLong() {
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
    public void rawBytes() {
        var array = new byte[]{0x0B, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64};
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        buffer.write(RAW_BYTES, array);
        assertEquals(0, buffer.readIndex());
        assertEquals(array.length, buffer.writeIndex());

        var readArray = buffer.read(RAW_BYTES);
        assertArrayEquals(array, readArray);
        assertEquals(array.length, buffer.readIndex());
    }

    @Test
    public void string() {
        assertBufferType(STRING, "Hello World", new byte[]{0x0B, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64});
    }

    @Test
    public void nbt() {
        assertBufferType(NetworkBuffer.NBT, intBinaryTag(5));
        assertBufferType(NetworkBuffer.NBT, CompoundBinaryTag.from(Map.of("key", intBinaryTag(5))));
    }

    @Test
    public void component() {
        assertBufferType(COMPONENT, Component.text("Hello world"));
    }

    @Test
    public void uuid() {
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
    public void optional() {
        assertBufferTypeOptional(BOOLEAN, null, new byte[]{0});
        assertBufferTypeOptional(BOOLEAN, true, new byte[]{1, 1});
    }

    @Test
    public void collection() {
        assertBufferTypeCollection(BOOLEAN, List.of(), new byte[]{0});
        assertBufferTypeCollection(BOOLEAN, List.of(true), new byte[]{0x01, 0x01});
    }

    @Test
    public void collectionMaxSize() {
        var buffer = NetworkBuffer.resizableBuffer();
        var list = new ArrayList<Boolean>();
        for (int i = 0; i < 1000; i++)
            list.add(true);
        buffer.write(BOOLEAN.list(), list);

        assertThrows(IllegalArgumentException.class, () -> buffer.read(BOOLEAN.list(10)));
    }

    @Test
    public void oomStringRegression() {
        var buffer = NetworkBuffer.resizableBuffer(100);
        buffer.write(VAR_INT, Integer.MAX_VALUE); // String length
        buffer.write(RAW_BYTES, "Hello".getBytes(StandardCharsets.UTF_8)); // String data

        assertThrows(IllegalArgumentException.class, () -> buffer.read(STRING)); // oom
    }

    static <T> void assertBufferType(NetworkBuffer.@NotNull Type<T> type, @UnknownNullability T value, byte[] expected, @NotNull Action<T> action) {
        var buffer = NetworkBuffer.resizableBuffer(MinecraftServer.process());
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
            var tmp = NetworkBuffer.resizableBuffer(0);
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
                buffer.write(type.optional(), value);
            }

            @Override
            public T read(@NotNull NetworkBuffer buffer, @NotNull NetworkBuffer.Type<T> type) {
                return buffer.read(type.optional());
            }
        });
    }

    static <T> void assertBufferTypeOptional(NetworkBuffer.@NotNull Type<T> type, @Nullable T value) {
        assertBufferTypeOptional(type, value, null);
    }

    static <T> void assertBufferTypeCollection(NetworkBuffer.@NotNull Type<T> type, @NotNull List<T> values, byte @Nullable [] expected) {
        var buffer = NetworkBuffer.resizableBuffer(MinecraftServer.process());
        buffer.write(type.list(), values);
        assertEquals(0, buffer.readIndex());
        if (expected != null) assertEquals(expected.length, buffer.writeIndex());

        var actual = buffer.read(type.list(Integer.MAX_VALUE));

        assertEquals(values, actual);
        if (expected != null) assertEquals(expected.length, buffer.readIndex());
        if (expected != null) assertEquals(expected.length, buffer.writeIndex());

        if (expected != null) {
            var bytes = new byte[expected.length];
            buffer.copyTo(0, bytes, 0, bytes.length);
            assertArrayEquals(expected, bytes, "Invalid bytes: " + Arrays.toString(expected) + " != " + Arrays.toString(bytes));
        }
    }

    static <T> void assertBufferTypeCollection(NetworkBuffer.@NotNull Type<T> type, @NotNull List<T> value) {
        assertBufferTypeCollection(type, value, null);
    }

    interface Action<T> {
        void write(@NotNull NetworkBuffer buffer, NetworkBuffer.@NotNull Type<T> type, @UnknownNullability T value);

        T read(@NotNull NetworkBuffer buffer, NetworkBuffer.@NotNull Type<T> type);
    }
}
