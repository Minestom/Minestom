package net.minestom.server.network;

import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;

interface NetworkBufferLayouts {
    ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;

    // Java BYTE and its array are not big endian
    ValueLayout.OfByte JAVA_BYTE = ValueLayout.JAVA_BYTE;
    ValueLayout.OfShort JAVA_SHORT = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(BYTE_ORDER);
    ValueLayout.OfInt JAVA_INT = ValueLayout.JAVA_INT_UNALIGNED.withOrder(BYTE_ORDER);
    ValueLayout.OfLong JAVA_LONG = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(BYTE_ORDER);
    ValueLayout.OfFloat JAVA_FLOAT = ValueLayout.JAVA_FLOAT_UNALIGNED.withOrder(BYTE_ORDER);
    ValueLayout.OfDouble JAVA_DOUBLE = ValueLayout.JAVA_DOUBLE_UNALIGNED.withOrder(BYTE_ORDER);
}
