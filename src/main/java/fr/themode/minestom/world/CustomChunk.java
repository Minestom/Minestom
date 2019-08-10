package fr.themode.minestom.world;

import java.util.HashMap;

public class CustomChunk {

    private CustomBiome biome;
    private HashMap<Short, CustomBlock> blocks;

    public CustomChunk(CustomBiome biome) {
        this.biome = biome;
        this.blocks = new HashMap<>();
    }

    public CustomBiome getBiome() {
        return biome;
    }

    public HashMap<Short, CustomBlock> getBlocks() {
        return blocks;
    }

    public void setBlock(int x, int y, int z, CustomBlock customBlock) {
        short index = (short) (x & 0x000F);
        index |= (y << 4) & 0x0FF0;
        index |= (z << 12) & 0xF000;
        this.blocks.put(index, customBlock);
    }

    public CustomBlock getBlock(int x, int y, int z) {
        short index = (short) (x & 0x000F);
        index |= (y << 4) & 0x0FF0;
        index |= (z << 12) & 0xF000;
        return this.blocks.getOrDefault(index, new CustomBlock(0));
    }

    @Override
    public String toString() {
        return String.format("CustomChunk{biome=%s, blocks=%s}", biome, blocks.values());
    }
}
