package net.minestom.server.instance.lighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;

/**
 * Configure chunk light data.
 * <p>
 * Notably used in {@link ChunkLightEngine#lightChunk(ChunkLight)} when implementing {@link ChunkLightEngine}
 */
public class ChunkLight {

    // Data
    final Chunk chunk;

    // List of sky's light levels
    List<byte[]> skyLight;

    // List of block's light levels
    List<byte[]> blockLight;

    public ChunkLight(Chunk chunk) {
        this.chunk = chunk;

        // Create a fixed sized list with a length of 18.
        this.skyLight = Arrays.asList(new byte[18][2048]);
        this.blockLight = Arrays.asList(new byte[18][2048]);
    }

    /**
     * Update the light level for a block at the given position
     * <p>
     * Ensure each coordinate is less than 16.
     * 
     * @param x Chunk block X
     * @param y Chunk block Y
     * @param z Chunk block Z
     * @param blockLightLevel Light emission level from 0 to 15
     */
    public void setBlockLight(int x, int y, int z, int blockLightLevel) {
        setLight(x, y, z, blockLightLevel, true);
    }

    /**
     * Update the light level for the sky at the given position
     * <p>
     * Ensure each coordinate is less than 16.
     * 
     * @param x Chunk block X
     * @param y Chunk block Y
     * @param z Chunk block Z
     * @param skyLightLevel Light emission level from 0 to 15
     */
    public void setSkyLight(int x, int y, int z, int skyLightLevel) {
        setLight(x, y, z, skyLightLevel, false);
    }

    // Sky and block uses same exact parser
    private void setLight(int x, int y, int z, int lightLevel, boolean isBlock) {
        // Stop a million errors in console
        boolean coordinatesCorrect = checkCoordinates(x, y, z);
        if(!coordinatesCorrect) {
            MinecraftServer.LOGGER.error("ChunkLight's coordinates are invalid! Must be less than 16 for each, got ("+x+", "+y+", "+z+")!");
            return;
        }
        
        // Get the chunk section
        List<byte[]> currentLightArrayList = isBlock ? blockLight : skyLight;
        byte[] currentLightArray = currentLightArrayList.get(y);

        // Add light emission value in array
        int coordinateIndex = getIndex(x, y, z);
        int positionIndex = coordinateIndex >> 1;

        if(isFirst(coordinateIndex))
            currentLightArray[positionIndex] = (byte)(currentLightArray[positionIndex] & 0xF0 | lightLevel & 0xF);
        else
            currentLightArray[positionIndex] = (byte)(currentLightArray[positionIndex] & 0xF | (lightLevel & 0xF) << 4);

        // Ensure the array is in the list
        currentLightArrayList.set(y, currentLightArray);
    }

    public int getChunkX() {
        return chunk.getChunkX();
    }

    public int getChunkZ() {
        return chunk.getChunkZ();
    }

    public UpdateLightPacket convertDataIntoPacket() {
        // Generate new data
        List<byte[]> compactBlockLightList = new ArrayList<>();
        List<byte[]> compactSkyLightList = new ArrayList<>();
        int skyLightMask = 0;
        int blockLightMask = 0;
        int emptySkyLightMask = 0;
        int emptyBlockLightMask = 0;

        for(int i = 0; i < 18; i++)
        {
            byte[] blockLightArray = blockLight.get(i);
            byte[] skyLightArray = skyLight.get(i);

            if(blockLightArray == null) {
                emptyBlockLightMask |= 1 << i;
            } else {
                blockLightMask |= 1 << i;
                compactBlockLightList.add(blockLightArray);
            }

            if(skyLightArray == null) {
                emptySkyLightMask |= 1 << i;
            } else {
                skyLightMask |= 1 << i;
                compactSkyLightList.add(skyLightArray);
            }
        }

        // Form a fresh packet
        UpdateLightPacket lightPacket = new UpdateLightPacket(chunk.getIdentifier(), chunk.getLastChangeTime());
        lightPacket.chunkX = chunk.getChunkX();
        lightPacket.chunkZ = chunk.getChunkZ();
        lightPacket.skyLightMask = skyLightMask;
        lightPacket.blockLightMask = blockLightMask;
        lightPacket.emptySkyLightMask = emptySkyLightMask;
        lightPacket.emptyBlockLightMask = emptyBlockLightMask;
        lightPacket.skyLight = compactSkyLightList;
        lightPacket.blockLight = compactBlockLightList;
        return lightPacket;
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