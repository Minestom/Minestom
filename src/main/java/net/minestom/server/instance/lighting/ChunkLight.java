package net.minestom.server.instance.lighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

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
    private final Chunk chunk;

    // Cache
    private UpdateLightPacket lightPacket;
    private boolean shouldUpdate;

    // List of sky's light levels
    private ChunkSectionLight[] chunkSectionData;

    public ChunkLight(Chunk chunk) {
        this.chunk = chunk;
        this.shouldUpdate = false;
        this.lightPacket = new UpdateLightPacket();

        // Create a fixed sized list with a length of 18.
        this.chunkSectionData = new ChunkSectionLight[18];
        Arrays.fill(chunkSectionData, new ChunkSectionLight());
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
            MinecraftServer.LOGGER.error("ChunkLight's coordinates are invalid! Must be less than 16 for X & Z and within -16 to 271 for Y, got ("+x+", "+y+", "+z+")!");
            return;
        }
        
        // Convert coordinate to chunk section coordinate based off of Update Light.
        int sectionIndex = (int) (Math.floor((y+16)/16));
        chunkSectionData[sectionIndex].setLight(x, (y%16), z, lightLevel, isBlock);
        this.shouldUpdate = true;
    }

    public int getChunkX() {
        return chunk.getChunkX();
    }

    public int getChunkZ() {
        return chunk.getChunkZ();
    }

    @NotNull
    public UpdateLightPacket getLightPacket()
    {
        return shouldUpdate ? updateLightPacket() : lightPacket;
    }

    public UpdateLightPacket updateLightPacket() {
        // Generate new data
        List<byte[]> compactBlockLightList = new ArrayList<>();
        List<byte[]> compactSkyLightList = new ArrayList<>();
        int skyLightMask = 0;
        int blockLightMask = 0;
        int emptySkyLightMask = 0;
        int emptyBlockLightMask = 0;

        for(int i = 0; i < 18; i++)
        {
            ChunkSectionLight sectionLight = chunkSectionData[i];
            byte[] blockLightArray = sectionLight.getBlockLightArray();
            byte[] skyLightArray = sectionLight.getSkyLightArray();

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
        
        this.lightPacket = lightPacket;
        this.shouldUpdate = false;
        return lightPacket;
    }

    private boolean checkCoordinates(int x, int y, int z)
    {
        return (x < 16 && x > -1) &&
            (y < 272 && y > -17) && // 2 extra sections for below bedrock and above height limit
            (z < 16 && z > -1);
    }

    
}