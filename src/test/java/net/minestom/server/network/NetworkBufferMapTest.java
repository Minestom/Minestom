package net.minestom.server.network;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NetworkBufferMapTest {

    @Test
    void defaultMapSizeCanExceedInitialAllocationCap() {
        final int entryCount = NetworkBufferTypeImpl.MAX_INITIAL_COLLECTION_SIZE + 1;
        final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        buffer.write(NetworkBuffer.VAR_INT, entryCount);
        for (int i = 0; i < entryCount; i++) {
            buffer.write(NetworkBuffer.INT, i);
            buffer.write(NetworkBuffer.INT, i);
        }

        final Map<Integer, Integer> map = buffer.read(NetworkBuffer.INT.mapValue(NetworkBuffer.INT));
        assertEquals(entryCount, map.size());
    }
}
