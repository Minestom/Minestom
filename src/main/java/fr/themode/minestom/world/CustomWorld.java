package fr.themode.minestom.world;

import java.util.HashMap;

public class CustomWorld {

    private String name;
    private HashMap<Integer, CustomChunk> chunks;

    public CustomWorld(String name) {
        this.name = name;
        this.chunks = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public HashMap<Integer, CustomChunk> getChunks() {
        return chunks;
    }

    public void setChunk(int x, int z, CustomChunk customChunk) {
        int index = x & 0x0000FFFF;
        index |= (z << 16) & 0xFFFF0000;
        this.chunks.put(index, customChunk);
    }

    public CustomChunk getChunk(int x, int z) {
        int index = x & 0x0000FFFF;
        index |= (z << 16) & 0xFFFF0000;
        return this.chunks.get(index);
    }

    public void setBlock(int x, int y, int z, CustomBlock customBlock) {
        int chunkX = x / 16;
        int chunkZ = z / 16;
        getChunk(chunkX, chunkZ).setBlock(x % 16, y, z % 16, customBlock);
    }

    public CustomBlock getBlock(int x, int y, int z) {
        int chunkX = x / 16;
        int chunkZ = z / 16;
        return getChunk(chunkX, chunkZ).getBlock(x % 16, y, z % 16);
    }

    @Override
    public String toString() {
        return String.format("CustomWorld{name=%s, chunks=%s}", name, chunks.values());
    }
}
