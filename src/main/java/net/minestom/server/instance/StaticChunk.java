package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.data.Data;
import net.minestom.server.instance.block.BlockProvider;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;

import java.util.HashSet;
import java.util.Set;

public class StaticChunk extends Chunk {

    protected final BlockProvider blockProvider;

    public StaticChunk(Instance instance, Biome[] biomes, int chunkX, int chunkZ, BlockProvider blockProvider) {
        super(instance, biomes, chunkX, chunkZ);
        this.blockProvider = blockProvider;
    }

    @Override
    public void UNSAFE_setBlock(int x, int y, int z, short blockStateId, short customBlockId, Data data, boolean updatable) {
        //noop
    }

    @Override
    public void tick(long time, Instance instance) {
        //noop
    }

    @Override
    public short getBlockStateId(int x, int y, int z) {
        return blockProvider.getBlockStateId(x, y, z);
    }

    @Override
    public short getCustomBlockId(int x, int y, int z) {
        //noop
        return 0;
    }

    @Override
    protected void refreshBlockValue(int x, int y, int z, short blockStateId, short customId) {
        //noop
    }

    @Override
    protected void refreshBlockStateId(int x, int y, int z, short blockStateId) {
        //noop
    }

    @Override
    public Data getBlockData(int index) {
        return null;
    }

    @Override
    public void setBlockData(int x, int y, int z, Data data) {
        //noop
    }

    @Override
    public Set<Integer> getBlockEntities() {
        return new HashSet<>();
    }

    @Override
    public byte[] getSerializedData() {
        return null;
    }

    @Override
    public void readChunk(BinaryReader reader, ChunkCallback callback) {
        callback.accept(this);
    }

    @Override
    protected ChunkDataPacket getFreshPacket() {
        ChunkDataPacket fullDataPacket = new ChunkDataPacket();
        fullDataPacket.biomes = biomes.clone();
        fullDataPacket.chunkX = chunkX;
        fullDataPacket.chunkZ = chunkZ;
        short[] blocksStateId = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z];
        for (int i = 0; i < blocksStateId.length; i++) {
            final int x = ChunkUtils.blockIndexToChunkPositionX(i);
            final int y = ChunkUtils.blockIndexToChunkPositionY(i);
            final int z = ChunkUtils.blockIndexToChunkPositionZ(i);
            blocksStateId[i] = blockProvider.getBlockStateId(x, y, z);
        }
        fullDataPacket.blocksStateId = blocksStateId;
        fullDataPacket.customBlocksId = new short[0];
        fullDataPacket.blockEntities = new HashSet<>();
        fullDataPacket.blocksData = new Int2ObjectOpenHashMap<>();
        return fullDataPacket;
    }

}