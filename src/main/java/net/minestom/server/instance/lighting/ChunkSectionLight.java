package net.minestom.server.instance.lighting;

import net.minestom.server.MinecraftServer;

/**
 * Configure chunk section light data.
 * <p>
 * Notably used in {@link ChunkLightEngine#lightChunk(ChunkLight)} when implementing {@link ChunkLightEngine}
 */
public class ChunkSectionLight {
    // Array of sky's light levels
    byte[] skyLightArray;

    // Array of block's light levels
    byte[] blockLightArray;

    public ChunkSectionLight() {
        this.skyLightArray = new byte[2048];
        this.blockLightArray = new byte[2048];
    }

    // Sky and block uses same exact parser
    public void setLight(int x, int y, int z, int lightLevel, boolean isBlock) {
        // Stop a million errors in console
        boolean coordinatesCorrect = checkCoordinates(x, y, z);
        if(!coordinatesCorrect) {
            MinecraftServer.LOGGER.error("ChunkSectionLight's coordinates are invalid! Must be less than 16 for each, got ("+x+", "+y+", "+z+")!");
            return;
        }
        byte[] currentLightArray = isBlock ? blockLightArray : skyLightArray;

        // Add light emission value in array
        int coordinateIndex = getIndex(x, y, z);
        int positionIndex = coordinateIndex >> 1;

        // Add data to the current mask
        if(isFirst(coordinateIndex))
            currentLightArray[positionIndex] = (byte)(currentLightArray[positionIndex] & 0xF0 | lightLevel & 0xF);
        else
            currentLightArray[positionIndex] = (byte)(currentLightArray[positionIndex] & 0xF | (lightLevel & 0xF) << 4);
        
        if(isBlock)
            blockLightArray = currentLightArray;
        else
            skyLightArray = currentLightArray;
    }

    public byte[] getSkyLightArray() {
        return this.skyLightArray;
    }

    public byte[] getBlockLightArray() {
        return this.blockLightArray;
    }

    private int getIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    private boolean isFirst(int coordinateIndex) {
        return ((coordinateIndex & 0x1) == 0);
    }

    private boolean checkCoordinates(int x, int y, int z)
    {
        return (x < 16 && x > -1) &&
            (y < 16 && y > -1) &&
            (z < 16 && z > -1);
    }

    
}