package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.data.Data;
import net.minestom.server.instance.block.BlockProvider;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a {@link Chunk} which does not store any block, it makes use of a {@link BlockProvider}
 * instead to use less memory.
 * <p>
 * Can be used for very simple chunks such as flat or others with not any random factor.
 * <p>
 * WARNING: adding blocks or anything to this chunk would not work, it is static.
 */
public class StaticChunk extends Chunk {

    protected final BlockProvider blockProvider;

    public StaticChunk(Instance instance, Biome[] biomes, int chunkX, int chunkZ, BlockProvider blockProvider) {
        super(instance, biomes, chunkX, chunkZ, false);
        this.blockProvider = blockProvider;
        setReadOnly(true);
    }

    @Override
    public void UNSAFE_setBlock(int x, int y, int z, short blockStateId, short customBlockId, Data data, boolean updatable) {
        //noop
    }

    @Override
    public void tick(long time, @NotNull Instance instance) {
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

    @NotNull
    @Override
    public Set<Integer> getBlockEntities() {
        return new HashSet<>();
    }

    @Override
    public byte[] getSerializedData() {
        return null;
    }

    @Override
    public void readChunk(@NotNull BinaryReader reader, @Nullable ChunkCallback callback) {
        OptionalCallback.execute(callback, this);
    }

    @NotNull
    @Override
    protected ChunkDataPacket createFreshPacket() {
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