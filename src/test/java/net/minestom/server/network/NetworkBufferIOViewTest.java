package net.minestom.server.network;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkBufferIOViewTest {

    @Test
    public void testWriteAndReadBoolean() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeBoolean(true);
        view.writeBoolean(false);

        assertTrue(view.readBoolean());
        assertFalse(view.readBoolean());
    }

    @Test
    public void testWriteAndReadByte() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeByte(42);
        view.writeByte(-128);
        view.writeByte(127);

        assertEquals(42, view.readByte());
        assertEquals(-128, view.readByte());
        assertEquals(127, view.readByte());
    }

    @Test
    public void testWriteAndReadUnsignedByte() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeByte(0);
        view.writeByte(255);
        view.writeByte(-1); // Should be read as 255

        assertEquals(0, view.readUnsignedByte());
        assertEquals(255, view.readUnsignedByte());
        assertEquals(255, view.readUnsignedByte());
    }

    @Test
    public void testWriteAndReadShort() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeShort(1234);
        view.writeShort(-32768);
        view.writeShort(32767);

        assertEquals(1234, view.readShort());
        assertEquals(-32768, view.readShort());
        assertEquals(32767, view.readShort());
    }

    @Test
    public void testWriteAndReadUnsignedShort() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeShort(0);
        view.writeShort(65535);
        view.writeShort(-1); // Should be read as 65535

        assertEquals(0, view.readUnsignedShort());
        assertEquals(65535, view.readUnsignedShort());
        assertEquals(65535, view.readUnsignedShort());
    }

    @Test
    public void testWriteAndReadChar() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeChar('A');
        view.writeChar('Z');
        view.writeChar('€');
        view.writeChar(0);
        view.writeChar(65535);

        assertEquals('A', view.readChar());
        assertEquals('Z', view.readChar());
        assertEquals('€', view.readChar());
        assertEquals((char) 0, view.readChar());
        assertEquals((char) 65535, view.readChar());
    }

    @Test
    public void testWriteAndReadInt() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeInt(123456);
        view.writeInt(-2147483648);
        view.writeInt(2147483647);
        view.writeInt(0);

        assertEquals(123456, view.readInt());
        assertEquals(-2147483648, view.readInt());
        assertEquals(2147483647, view.readInt());
        assertEquals(0, view.readInt());
    }

    @Test
    public void testWriteAndReadLong() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeLong(123456789012345L);
        view.writeLong(-9223372036854775808L);
        view.writeLong(9223372036854775807L);
        view.writeLong(0L);

        assertEquals(123456789012345L, view.readLong());
        assertEquals(-9223372036854775808L, view.readLong());
        assertEquals(9223372036854775807L, view.readLong());
        assertEquals(0L, view.readLong());
    }

    @Test
    public void testWriteAndReadFloat() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeFloat(3.14159f);
        view.writeFloat(-0.0f);
        view.writeFloat(Float.MAX_VALUE);
        view.writeFloat(Float.MIN_VALUE);
        view.writeFloat(Float.NaN);
        view.writeFloat(Float.POSITIVE_INFINITY);

        assertEquals(3.14159f, view.readFloat(), 0.00001f);
        assertEquals(-0.0f, view.readFloat());
        assertEquals(Float.MAX_VALUE, view.readFloat());
        assertEquals(Float.MIN_VALUE, view.readFloat());
        assertTrue(Float.isNaN(view.readFloat()));
        assertEquals(Float.POSITIVE_INFINITY, view.readFloat());
    }

    @Test
    public void testWriteAndReadDouble() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeDouble(3.141592653589793);
        view.writeDouble(-0.0);
        view.writeDouble(Double.MAX_VALUE);
        view.writeDouble(Double.MIN_VALUE);
        view.writeDouble(Double.NaN);
        view.writeDouble(Double.NEGATIVE_INFINITY);

        assertEquals(3.141592653589793, view.readDouble(), 0.000000000000001);
        assertEquals(-0.0, view.readDouble());
        assertEquals(Double.MAX_VALUE, view.readDouble());
        assertEquals(Double.MIN_VALUE, view.readDouble());
        assertTrue(Double.isNaN(view.readDouble()));
        assertEquals(Double.NEGATIVE_INFINITY, view.readDouble());
    }

    @Test
    public void testWriteAndReadSingleByte() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.write(65); // 'A'
        view.write(255);
        view.write(-1); // Should write as byte

        assertEquals(65, view.readByte());
        assertEquals((byte) 255, view.readByte());
        assertEquals((byte) -1, view.readByte());
    }

    @Test
    public void testWriteAndReadByteArray() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        byte[] data = {1, 2, 3, 4, 5};
        view.write(data);

        byte[] result = new byte[5];
        view.readFully(result);

        assertArrayEquals(data, result);
    }

    @Test
    public void testWriteAndReadByteArrayWithOffset() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        byte[] data = {1, 2, 3, 4, 5, 6, 7};
        view.write(data, 2, 3); // Write bytes at index 2-4

        byte[] result = new byte[3];
        view.readFully(result);

        assertArrayEquals(new byte[]{3, 4, 5}, result);
    }

    @Test
    public void testReadFullyWithOffset() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        byte[] data = {10, 20, 30, 40, 50};
        view.write(data);

        byte[] result = new byte[10];
        view.readFully(result, 2, 5);

        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
        assertEquals(10, result[2]);
        assertEquals(20, result[3]);
        assertEquals(30, result[4]);
        assertEquals(40, result[5]);
        assertEquals(50, result[6]);
        assertEquals(0, result[7]);
    }

    @Test
    public void testSkipBytes() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeInt(100);
        view.writeInt(200);
        view.writeInt(300);

        assertEquals(100, view.readInt());
        assertEquals(4, view.skipBytes(4)); // Skip the second int
        assertEquals(300, view.readInt());
    }

    @Test
    public void testSkipBytesPartial() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeInt(100);
        view.writeInt(200);

        view.readInt(); // Read first int
        int skipped = view.skipBytes(100); // Try to skip more than available
        assertEquals(4, skipped); // Only 4 bytes (one int) left
    }

    @Test
    public void testSkipBytesZero() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeInt(100);

        assertEquals(0, view.skipBytes(0));
        assertEquals(100, view.readInt());
    }

    @Test
    public void testWriteAndReadUTF() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeUTF("Hello, World!");
        view.writeUTF("Test 123");
        view.writeUTF("€ ¥ £ 中文");

        assertEquals("Hello, World!", view.readUTF());
        assertEquals("Test 123", view.readUTF());
        assertEquals("€ ¥ £ 中文", view.readUTF());
    }

    @Test
    public void testWriteAndReadUTFEmpty() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeUTF("");

        assertEquals("", view.readUTF());
    }

    @Test
    public void testWriteBytes() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeBytes("ABC");

        assertEquals((byte) 'A', view.readByte());
        assertEquals((byte) 'B', view.readByte());
        assertEquals((byte) 'C', view.readByte());
    }

    @Test
    public void testWriteBytesLowByteOnly() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        // writeBytes should only write the low byte of each char
        view.writeBytes("€"); // Euro sign (U+20AC = 0x20AC)

        // Only the low byte (0xAC) should be written
        assertEquals((byte) 0xAC, view.readByte());
    }

    @Test
    public void testWriteChars() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeChars("AB");

        assertEquals('A', view.readChar());
        assertEquals('B', view.readChar());
    }

    @Test
    public void testWriteCharsUnicode() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        view.writeChars("€¥");

        assertEquals('€', view.readChar());
        assertEquals('¥', view.readChar());
    }

    @Test
    public void testWriteUTFNull() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        assertThrows(NullPointerException.class, () -> view.writeUTF(null));
    }

    @Test
    public void testWriteBytesNull() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        assertThrows(NullPointerException.class, () -> view.writeBytes(null));
    }

    @Test
    public void testWriteCharsNull() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        assertThrows(NullPointerException.class, () -> view.writeChars(null));
    }

    @Test
    public void testMixedReadWrite() {
        var buffer = NetworkBuffer.resizableBuffer();
        var view = buffer.ioView();

        // Write mixed data types
        view.writeBoolean(true);
        view.writeByte(42);
        view.writeShort(1000);
        view.writeChar('X');
        view.writeInt(123456);
        view.writeLong(987654321L);
        view.writeFloat(3.14f);
        view.writeDouble(2.718);
        view.writeUTF("Test");

        // Read back in same order
        assertTrue(view.readBoolean());
        assertEquals(42, view.readByte());
        assertEquals(1000, view.readShort());
        assertEquals('X', view.readChar());
        assertEquals(123456, view.readInt());
        assertEquals(987654321L, view.readLong());
        assertEquals(3.14f, view.readFloat(), 0.001f);
        assertEquals(2.718, view.readDouble(), 0.0001);
        assertEquals("Test", view.readUTF());
    }

    @Test
    public void testBufferNotNull() {
        assertThrows(NullPointerException.class, () -> new NetworkBufferIOViewImpl(null));
    }

    // Tests comparing against Java's DataInputStream/DataOutputStream

    @Test
    public void testCompatibilityWithDataOutputStream() throws Exception {
        // Write using standard Java DataOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeBoolean(true);
        dos.writeByte(42);
        dos.writeShort(1234);
        dos.writeChar('X');
        dos.writeInt(123456);
        dos.writeLong(987654321L);
        dos.writeFloat(3.14f);
        dos.writeDouble(2.718);
        dos.writeUTF("Hello");
        dos.flush();

        byte[] javaBytes = baos.toByteArray();

        // Write using NetworkBufferIOView and extract bytes
        byte[] networkBytes = NetworkBuffer.resizableBuffer().extractWrittenBytes(buffer -> {
            var view = buffer.ioView();
            view.writeBoolean(true);
            view.writeByte(42);
            view.writeShort(1234);
            view.writeChar('X');
            view.writeInt(123456);
            view.writeLong(987654321L);
            view.writeFloat(3.14f);
            view.writeDouble(2.718);
            view.writeUTF("Hello");
        });

        assertArrayEquals(javaBytes, networkBytes, "NetworkBuffer bytes should match Java DataOutputStream bytes");
    }

    @Test
    public void testCompatibilityWithDataInputStream() throws Exception {
        // Write using NetworkBufferIOView and extract bytes
        byte[] networkBytes = NetworkBuffer.resizableBuffer().extractWrittenBytes(buffer -> {
            var view = buffer.ioView();
            view.writeBoolean(false);
            view.writeByte(-5);
            view.writeShort(-1000);
            view.writeChar('€');
            view.writeInt(-987654);
            view.writeLong(-123456789L);
            view.writeFloat(-2.5f);
            view.writeDouble(-3.141592);
            view.writeUTF("Test String");
        });

        // Read using standard Java DataInputStream
        ByteArrayInputStream bais = new ByteArrayInputStream(networkBytes);
        DataInputStream dis = new DataInputStream(bais);

        assertFalse(dis.readBoolean());
        assertEquals(-5, dis.readByte());
        assertEquals(-1000, dis.readShort());
        assertEquals('€', dis.readChar());
        assertEquals(-987654, dis.readInt());
        assertEquals(-123456789L, dis.readLong());
        assertEquals(-2.5f, dis.readFloat(), 0.0001f);
        assertEquals(-3.141592, dis.readDouble(), 0.000001);
        assertEquals("Test String", dis.readUTF());
    }

    @Test
    public void testReadFromJavaDataOutputStream() throws Exception {
        // Write using Java DataOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeBoolean(true);
        dos.writeByte(100);
        dos.writeShort(20000);
        dos.writeChar('Z');
        dos.writeInt(1000000);
        dos.writeLong(9999999999L);
        dos.writeFloat(1.5f);
        dos.writeDouble(9.876);
        dos.writeUTF("Java UTF");
        dos.flush();

        byte[] javaBytes = baos.toByteArray();

        // Load into NetworkBuffer and read
        var buffer = NetworkBuffer.wrap(javaBytes, 0, javaBytes.length);
        var view = buffer.ioView();

        assertTrue(view.readBoolean());
        assertEquals(100, view.readByte());
        assertEquals(20000, view.readShort());
        assertEquals('Z', view.readChar());
        assertEquals(1000000, view.readInt());
        assertEquals(9999999999L, view.readLong());
        assertEquals(1.5f, view.readFloat(), 0.0001f);
        assertEquals(9.876, view.readDouble(), 0.000001);
        assertEquals("Java UTF", view.readUTF());
    }

    @Test
    public void testWriteBytesCompatibility() throws Exception {
        String testString = "Hello World!";

        // Java DataOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeBytes(testString);
        dos.flush();
        byte[] javaBytes = baos.toByteArray();

        // NetworkBuffer
        byte[] networkBytes = NetworkBuffer.resizableBuffer().extractWrittenBytes(buffer -> {
            var view = buffer.ioView();
            view.writeBytes(testString);
        });

        assertArrayEquals(javaBytes, networkBytes);
    }

    @Test
    public void testWriteCharsCompatibility() throws Exception {
        String testString = "Test€¥";

        // Java DataOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeChars(testString);
        dos.flush();
        byte[] javaBytes = baos.toByteArray();

        // NetworkBuffer
        byte[] networkBytes = NetworkBuffer.resizableBuffer().extractWrittenBytes(buffer -> {
            var view = buffer.ioView();
            view.writeChars(testString);
        });

        assertArrayEquals(javaBytes, networkBytes);
    }

    @Test
    public void testUnsignedByteCompatibility() throws Exception {
        // Test unsigned byte reading
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(0);
        dos.writeByte(127);
        dos.writeByte(128); // -128 as signed, 128 as unsigned
        dos.writeByte(255); // -1 as signed, 255 as unsigned
        dos.flush();

        byte[] javaBytes = baos.toByteArray();

        // Read with Java
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(javaBytes));
        int java1 = dis.readUnsignedByte();
        int java2 = dis.readUnsignedByte();
        int java3 = dis.readUnsignedByte();
        int java4 = dis.readUnsignedByte();

        // Read with NetworkBuffer
        var buffer = NetworkBuffer.wrap(javaBytes, 0, javaBytes.length);
        var view = buffer.ioView();

        assertEquals(java1, view.readUnsignedByte());
        assertEquals(java2, view.readUnsignedByte());
        assertEquals(java3, view.readUnsignedByte());
        assertEquals(java4, view.readUnsignedByte());
    }

    @Test
    public void testUnsignedShortCompatibility() throws Exception {
        // Test unsigned short reading
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeShort(0);
        dos.writeShort(32767);
        dos.writeShort(32768); // -32768 as signed, 32768 as unsigned
        dos.writeShort(65535); // -1 as signed, 65535 as unsigned
        dos.flush();

        byte[] javaBytes = baos.toByteArray();

        // Read with Java
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(javaBytes));
        int java1 = dis.readUnsignedShort();
        int java2 = dis.readUnsignedShort();
        int java3 = dis.readUnsignedShort();
        int java4 = dis.readUnsignedShort();

        // Read with NetworkBuffer
        var buffer = NetworkBuffer.wrap(javaBytes, 0, javaBytes.length);
        var view = buffer.ioView();

        assertEquals(java1, view.readUnsignedShort());
        assertEquals(java2, view.readUnsignedShort());
        assertEquals(java3, view.readUnsignedShort());
        assertEquals(java4, view.readUnsignedShort());
    }

    @Test
    public void testReadFullyCompatibility() throws Exception {
        byte[] testData = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        // Test with Java DataInputStream
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(testData));
        byte[] javaResult = new byte[10];
        dis.readFully(javaResult);

        // Test with NetworkBuffer
        var buffer = NetworkBuffer.wrap(testData, 0, testData.length);
        var view = buffer.ioView();
        byte[] networkResult = new byte[10];
        view.readFully(networkResult);

        assertArrayEquals(javaResult, networkResult);
    }

    @Test
    public void testReadFullyWithOffsetCompatibility() throws Exception {
        byte[] testData = {10, 20, 30, 40, 50};

        // Test with Java DataInputStream
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(testData));
        byte[] javaResult = new byte[10];
        dis.readFully(javaResult, 2, 5);

        // Test with NetworkBuffer
        var buffer = NetworkBuffer.wrap(testData, 0, testData.length);
        var view = buffer.ioView();
        byte[] networkResult = new byte[10];
        view.readFully(networkResult, 2, 5);

        assertArrayEquals(javaResult, networkResult);
    }

    @Test
    public void testSkipBytesCompatibility() throws Exception {
        byte[] testData = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        // Test with Java DataInputStream
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(testData));
        dis.readByte(); // Read first byte
        int javaSkipped = dis.skipBytes(5);
        byte javaNext = dis.readByte();

        // Test with NetworkBuffer
        var buffer = NetworkBuffer.wrap(testData, 0, testData.length);
        var view = buffer.ioView();
        view.readByte(); // Read first byte
        int networkSkipped = view.skipBytes(5);
        byte networkNext = view.readByte();

        assertEquals(javaSkipped, networkSkipped);
        assertEquals(javaNext, networkNext);
    }

    @Test
    public void testWriteArrayWithOffsetCompatibility() throws Exception {
        byte[] testData = {1, 2, 3, 4, 5, 6, 7};

        // Test with Java DataOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.write(testData, 2, 3);
        dos.flush();
        byte[] javaBytes = baos.toByteArray();

        // Test with NetworkBuffer
        byte[] networkBytes = NetworkBuffer.resizableBuffer().extractWrittenBytes(buffer -> {
            var view = buffer.ioView();
            view.write(testData, 2, 3);
        });

        assertArrayEquals(javaBytes, networkBytes);
    }

    @Test
    public void testSpecialFloatValuesCompatibility() throws Exception {
        // Test special float values
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeFloat(Float.NaN);
        dos.writeFloat(Float.POSITIVE_INFINITY);
        dos.writeFloat(Float.NEGATIVE_INFINITY);
        dos.writeFloat(0.0f);
        dos.writeFloat(-0.0f);
        dos.flush();

        byte[] javaBytes = baos.toByteArray();

        // Write with NetworkBuffer
        byte[] networkBytes = NetworkBuffer.resizableBuffer().extractWrittenBytes(buffer -> {
            var view = buffer.ioView();
            view.writeFloat(Float.NaN);
            view.writeFloat(Float.POSITIVE_INFINITY);
            view.writeFloat(Float.NEGATIVE_INFINITY);
            view.writeFloat(0.0f);
            view.writeFloat(-0.0f);
        });

        assertArrayEquals(javaBytes, networkBytes);
    }

    @Test
    public void testSpecialDoubleValuesCompatibility() throws Exception {
        // Test special double values
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeDouble(Double.NaN);
        dos.writeDouble(Double.POSITIVE_INFINITY);
        dos.writeDouble(Double.NEGATIVE_INFINITY);
        dos.writeDouble(0.0);
        dos.writeDouble(-0.0);
        dos.flush();

        byte[] javaBytes = baos.toByteArray();

        // Write with NetworkBuffer
        byte[] networkBytes = NetworkBuffer.resizableBuffer().extractWrittenBytes(buffer -> {
            var view = buffer.ioView();
            view.writeDouble(Double.NaN);
            view.writeDouble(Double.POSITIVE_INFINITY);
            view.writeDouble(Double.NEGATIVE_INFINITY);
            view.writeDouble(0.0);
            view.writeDouble(-0.0);
        });

        assertArrayEquals(javaBytes, networkBytes);
    }

    @Test
    public void testComplexDataStreamCompatibility() throws Exception {
        // Write a complex sequence with Java
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF("Header");
        dos.writeInt(5); // Array length
        for (int i = 0; i < 5; i++) {
            dos.writeDouble(i * 1.5);
        }
        dos.writeBoolean(true);
        dos.writeUTF("Footer");
        dos.flush();

        byte[] javaBytes = baos.toByteArray();

        // Read with NetworkBuffer and verify
        var buffer = NetworkBuffer.wrap(javaBytes, 0, javaBytes.length);
        var view = buffer.ioView();

        assertEquals("Header", view.readUTF());
        assertEquals(5, view.readInt());
        for (int i = 0; i < 5; i++) {
            assertEquals(i * 1.5, view.readDouble(), 0.000001);
        }
        assertTrue(view.readBoolean());
        assertEquals("Footer", view.readUTF());

        // Verify we've read all data
        assertEquals(0, buffer.readableBytes());
    }
}
