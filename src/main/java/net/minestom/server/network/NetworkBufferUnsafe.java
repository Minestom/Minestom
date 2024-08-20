package net.minestom.server.network;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;

final class NetworkBufferUnsafe {
    static final Unsafe UNSAFE;

    static final Field ADDRESS, CAPACITY;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            ADDRESS = Buffer.class.getDeclaredField("address");
            CAPACITY = Buffer.class.getDeclaredField("capacity");
            // Use Unsafe to read value of the address field. This way it will not fail on JDK9+ which
            // will forbid changing the access level via reflection.
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * The offset, in bytes, between the base memory address of a byte array and its first element.
     */
    static final long BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);

    static void updateAddress(ByteBuffer buffer, long address) {
        final long offset = UNSAFE.objectFieldOffset(ADDRESS);
        UNSAFE.putLong(buffer, offset, address);
    }

    static void updateCapacity(ByteBuffer buffer, int capacity) {
        final long offset = UNSAFE.objectFieldOffset(CAPACITY);
        UNSAFE.putInt(buffer, offset, capacity);
    }
}
