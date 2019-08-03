package fr.themode.minestom.utils;

import fr.adamaq01.ozao.net.Buffer;

import java.io.UnsupportedEncodingException;

public class Utils {

    public static void writeString(Buffer buffer, String value) {
        byte[] bytes = new byte[0];
        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (bytes.length > 32767) {
            System.out.println("String too big (was " + value.length() + " bytes encoded, max " + 32767 + ")");
        } else {
            writeVarInt(buffer, bytes.length);
            buffer.putBytes(bytes);
        }
    }

    public static String readString(Buffer buffer) {
        int length = readVarInt(buffer);
        byte bytes[] = buffer.getBytes(length);
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeVarInt(Buffer buffer, int value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buffer.putByte(temp);
        } while (value != 0);
    }

    public static int readVarInt(Buffer buffer) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buffer.getByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    // ??
    public static int lengthVarInt(int value) {
        int i = 0;
        do {
            i++;
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
        } while (value != 0);
        return i;
    }

    public static void writeVarLong(Buffer buffer, long value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buffer.putByte(temp);
        } while (value != 0);
    }

    public static long readVarLong(Buffer buffer) {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = buffer.getByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 10) {
                throw new RuntimeException("VarLong is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static void writePosition(Buffer buffer, int x, int y, int z) {
        buffer.putLong(((x & 0x3FFFFFF) << 38) | ((y & 0xFFF) << 26) | (z & 0x3FFFFFF));
    }

}
