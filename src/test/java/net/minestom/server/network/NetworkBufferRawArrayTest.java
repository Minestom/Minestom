package net.minestom.server.network;

import org.junit.jupiter.api.Test;

import static net.minestom.server.network.NetworkBuffer.INT;
import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.RAW_DOUBLES;
import static net.minestom.server.network.NetworkBuffer.RAW_FLOATS;
import static net.minestom.server.network.NetworkBuffer.RAW_INTS;
import static net.minestom.server.network.NetworkBuffer.RAW_LONGS;
import static net.minestom.server.network.NetworkBuffer.RAW_SHORTS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NetworkBufferRawArrayTest {

    @Test
    public void bytes() {
        var values = new byte[]{0x00, 0x7F, (byte) 0x80, (byte) 0xFF};

        var buffer = NetworkBuffer.staticBuffer(4);
        buffer.write(RAW_BYTES, values);
        assertEquals(values.length, buffer.writeIndex());
        assertArrayEquals(values, written(buffer));
        assertEquals(values.length, RAW_BYTES.sizeOf(values));

        assertArrayEquals(values, buffer.read(RAW_BYTES));
        assertEquals(values.length, buffer.readIndex());
    }

    @Test
    public void shorts() {
        var values = new short[]{0, (short) 0x7FFF, (short) 0x8000, -1};
        var expected = new byte[]{0x00, 0x00, 0x7F, (byte) 0xFF, (byte) 0x80, 0x00, (byte) 0xFF, (byte) 0xFF};

        var buffer = NetworkBuffer.staticBuffer(8);
        buffer.write(RAW_SHORTS, values);
        assertEquals(expected.length, buffer.writeIndex());
        assertArrayEquals(expected, written(buffer));
        assertEquals(expected.length, RAW_SHORTS.sizeOf(values));

        assertArrayEquals(values, buffer.read(RAW_SHORTS));
        assertEquals(expected.length, buffer.readIndex());
    }

    @Test
    public void ints() {
        var values = new int[]{0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1};
        var expected = new byte[]{
                0x00, 0x00, 0x00, 0x00,
                0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x80, 0x00, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

        var buffer = NetworkBuffer.staticBuffer(16);
        buffer.write(RAW_INTS, values);
        assertEquals(expected.length, buffer.writeIndex());
        assertArrayEquals(expected, written(buffer));
        assertEquals(expected.length, RAW_INTS.sizeOf(values));

        assertArrayEquals(values, buffer.read(RAW_INTS));
        assertEquals(expected.length, buffer.readIndex());
    }

    @Test
    public void longs() {
        var values = new long[]{0L, Long.MAX_VALUE, Long.MIN_VALUE};
        var expected = new byte[]{
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        var buffer = NetworkBuffer.staticBuffer(24);
        buffer.write(RAW_LONGS, values);
        assertEquals(expected.length, buffer.writeIndex());
        assertArrayEquals(expected, written(buffer));
        assertEquals(expected.length, RAW_LONGS.sizeOf(values));

        assertArrayEquals(values, buffer.read(RAW_LONGS));
        assertEquals(expected.length, buffer.readIndex());
    }

    @Test
    public void floats() {
        var values = new float[]{1f, -2f, 0f, Float.NaN};
        var expected = new byte[]{
                0x3F, (byte) 0x80, 0x00, 0x00,
                (byte) 0xC0, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x7F, (byte) 0xC0, 0x00, 0x00};

        var buffer = NetworkBuffer.staticBuffer(16);
        buffer.write(RAW_FLOATS, values);
        assertEquals(expected.length, buffer.writeIndex());
        assertArrayEquals(expected, written(buffer));
        assertEquals(expected.length, RAW_FLOATS.sizeOf(values));

        assertArrayEquals(values, buffer.read(RAW_FLOATS));
        assertEquals(expected.length, buffer.readIndex());
    }

    @Test
    public void doubles() {
        var values = new double[]{1d, -2d, 0d, Double.NaN};
        var expected = new byte[]{
                0x3F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                (byte) 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x7F, (byte) 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        var buffer = NetworkBuffer.staticBuffer(32);
        buffer.write(RAW_DOUBLES, values);
        assertEquals(expected.length, buffer.writeIndex());
        assertArrayEquals(expected, written(buffer));
        assertEquals(expected.length, RAW_DOUBLES.sizeOf(values));

        assertArrayEquals(values, buffer.read(RAW_DOUBLES));
        assertEquals(expected.length, buffer.readIndex());
    }

    @Test
    public void empty() {
        var buffer = NetworkBuffer.staticBuffer(0);
        buffer.write(RAW_BYTES, new byte[0]);
        buffer.write(RAW_SHORTS, new short[0]);
        buffer.write(RAW_INTS, new int[0]);
        buffer.write(RAW_LONGS, new long[0]);
        buffer.write(RAW_FLOATS, new float[0]);
        buffer.write(RAW_DOUBLES, new double[0]);
        assertEquals(0, buffer.writeIndex());

        assertArrayEquals(new byte[0], buffer.read(RAW_BYTES));
        assertArrayEquals(new short[0], buffer.read(RAW_SHORTS));
        assertArrayEquals(new int[0], buffer.read(RAW_INTS));
        assertArrayEquals(new long[0], buffer.read(RAW_LONGS));
        assertArrayEquals(new float[0], buffer.read(RAW_FLOATS));
        assertArrayEquals(new double[0], buffer.read(RAW_DOUBLES));
        assertEquals(0, buffer.readIndex());
    }

    @Test
    public void resize() {
        var buffer = NetworkBuffer.resizableBuffer(0);
        buffer.write(RAW_BYTES, new byte[]{1});
        buffer.write(RAW_SHORTS, new short[]{2});
        buffer.write(RAW_INTS, new int[]{3});
        buffer.write(RAW_LONGS, new long[]{4L});
        buffer.write(RAW_FLOATS, new float[]{5f});
        buffer.write(RAW_DOUBLES, new double[]{6d});
        assertEquals(27, buffer.writeIndex());

        assertArrayEquals(new byte[]{1}, buffer.read(NetworkBuffer.FixedRawBytes(1)));
        assertArrayEquals(new short[]{2}, buffer.read(NetworkBuffer.FixedRawShorts(1)));
        assertArrayEquals(new int[]{3}, buffer.read(NetworkBuffer.FixedRawInts(1)));
        assertArrayEquals(new long[]{4L}, buffer.read(NetworkBuffer.FixedRawLongs(1)));
        assertArrayEquals(new float[]{5f}, buffer.read(NetworkBuffer.FixedRawFloats(1)));
        assertArrayEquals(new double[]{6d}, buffer.read(NetworkBuffer.FixedRawDoubles(1)));
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    public void unalignedRemainder() {
        var buffer = NetworkBuffer.staticBuffer(20);
        buffer.write(INT, 7);
        buffer.write(RAW_LONGS, new long[]{1L, 2L});

        assertEquals(7, buffer.read(INT));
        assertArrayEquals(new long[]{1L, 2L}, buffer.read(RAW_LONGS));
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    public void trailingPartialElement() {
        var buffer = NetworkBuffer.wrap(new byte[]{0x00, 0x01, 0x00, 0x02, 0x03}, 0, 5);

        assertArrayEquals(new short[]{1, 2}, buffer.read(RAW_SHORTS));
        assertEquals(4, buffer.readIndex());
        assertEquals(1, buffer.readableBytes());
    }

    @Test
    public void fixedLength() {
        var buffer = NetworkBuffer.staticBuffer(14);
        buffer.write(NetworkBuffer.FixedRawBytes(2), new byte[]{1, 2});
        buffer.write(NetworkBuffer.FixedRawInts(2), new int[]{1, 2});
        buffer.write(INT, 3);

        assertArrayEquals(new byte[]{1, 2}, buffer.read(NetworkBuffer.FixedRawBytes(2)));
        assertArrayEquals(new int[]{1, 2}, buffer.read(NetworkBuffer.FixedRawInts(2)));
        assertEquals(3, buffer.read(INT));
    }

    @Test
    public void fixedLengthWriteMismatch() {
        var buffer = NetworkBuffer.staticBuffer(0);
        assertThrows(IllegalArgumentException.class, () -> buffer.write(NetworkBuffer.FixedRawBytes(2), new byte[]{1}));
        assertThrows(IllegalArgumentException.class, () -> buffer.write(NetworkBuffer.FixedRawShorts(2), new short[]{1}));
        assertThrows(IllegalArgumentException.class, () -> buffer.write(NetworkBuffer.FixedRawInts(2), new int[]{1, 2, 3}));
        assertThrows(IllegalArgumentException.class, () -> buffer.write(NetworkBuffer.FixedRawLongs(1), new long[0]));
        assertThrows(IllegalArgumentException.class, () -> buffer.write(NetworkBuffer.FixedRawFloats(1), new float[]{1f, 2f}));
        assertThrows(IllegalArgumentException.class, () -> buffer.write(NetworkBuffer.FixedRawDoubles(3), new double[]{1d}));
        assertEquals(0, buffer.writeIndex());
    }

    @Test
    public void fixedLengthReadUnderflow() {
        var buffer = NetworkBuffer.wrap(new byte[]{0x01, 0x02, 0x03}, 0, 3);
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.read(NetworkBuffer.FixedRawBytes(4)));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.read(NetworkBuffer.FixedRawShorts(2)));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.read(NetworkBuffer.FixedRawInts(1)));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.read(NetworkBuffer.FixedRawLongs(1)));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.read(NetworkBuffer.FixedRawFloats(1)));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.read(NetworkBuffer.FixedRawDoubles(1)));
        assertEquals(0, buffer.readIndex());
    }

    @Test
    public void fixedLengthNegative() {
        assertThrows(IllegalArgumentException.class, () -> NetworkBuffer.FixedRawBytes(-1));
        assertThrows(IllegalArgumentException.class, () -> NetworkBuffer.FixedRawShorts(-1));
        assertThrows(IllegalArgumentException.class, () -> NetworkBuffer.FixedRawInts(-1));
        assertThrows(IllegalArgumentException.class, () -> NetworkBuffer.FixedRawLongs(-1));
        assertThrows(IllegalArgumentException.class, () -> NetworkBuffer.FixedRawFloats(-1));
        assertThrows(IllegalArgumentException.class, () -> NetworkBuffer.FixedRawDoubles(-1));
        assertThrows(IllegalArgumentException.class, () -> NetworkBuffer.FixedBitSet(-1));
    }

    private static byte[] written(NetworkBuffer buffer) {
        final byte[] bytes = new byte[Math.toIntExact(buffer.writeIndex())];
        buffer.copyTo(0, bytes, 0, bytes.length);
        return bytes;
    }
}
