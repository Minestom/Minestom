package net.minestom.server.instance.heightmap;

import net.minestom.server.network.NetworkBuffer;

public enum HeightmapType {
    WORLD_SURFACE_WG,
    WORLD_SURFACE,
    OCEAN_FLOOR_WG,
    OCEAN_FLOOR,
    MOTION_BLOCKING,
    MOTION_BLOCKING_NO_LEAVES;

    public static final NetworkBuffer.Type<HeightmapType> NETWORK_TYPE = NetworkBuffer.Enum(HeightmapType.class);

    /**
     * Creates compressed longs array from uncompressed heights array.
     *
     * @param heights      array of heights. Note that for this method it doesn't
     *                     matter what size this array will be.
     *                     But to get correct heights, array must be 256 elements
     *                     long, and at index `i` must be height of (z=i/16,
     *                     x=i%16).
     * @param bitsPerEntry bits that each entry from height will take in `long`
     *                     container.
     * @return array of encoded heights.
     */
    public static long[] encode(short[] heights, int bitsPerEntry) {
        final int entriesPerLong = 64 / bitsPerEntry;
        // ceil(HeightsCount / entriesPerLong)
        final int len = (heights.length + entriesPerLong - 1) / entriesPerLong;

        final int maxPossibleIndexInContainer = entriesPerLong - 1;
        final int entryMask = (1 << bitsPerEntry) - 1;

        long[] data = new long[len];
        int containerIndex = 0;
        for (int i = 0; i < heights.length; i++) {
            final int indexInContainer = i % entriesPerLong;
            final int entry = heights[i];
            data[containerIndex] |= ((long) (entry & entryMask)) << (indexInContainer * bitsPerEntry);
            if (indexInContainer == maxPossibleIndexInContainer) containerIndex++;
        }
        return data;
    }
}
