package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.data.Data;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.UpdateConsumer;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

public class StaticChunk extends Chunk {

	final Function<BlockPosition, Short> blockProvider;

	public StaticChunk(Biome[] biomes, int chunkX, int chunkZ, Function<BlockPosition, Short> blockProvider) {
		super(biomes, chunkX, chunkZ);
		this.blockProvider = blockProvider;
	}

	@Override
	public void UNSAFE_removeCustomBlock(int x, int y, int z) {
		//noop
	}

	@Override
	protected void setBlock(int x, int y, int z, short blockStateId, short customId, Data data, UpdateConsumer updateConsumer) {
		//noop
	}

	@Override
	public short getBlockStateId(int x, int y, int z) {
		return blockProvider.apply(new BlockPosition(x, y, z));
	}

	@Override
	public short getCustomBlockId(int x, int y, int z) {
		//noop
		return 0;
	}

	@Override
	public CustomBlock getCustomBlock(int x, int y, int z) {
		//noop
		return null;
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
	protected byte[] getSerializedData(){
		return null;
	}

	@Override
	protected ChunkDataPacket getFreshPacket() {
		ChunkDataPacket fullDataPacket = new ChunkDataPacket();
		fullDataPacket.biomes = biomes.clone();
		fullDataPacket.chunkX = chunkX;
		fullDataPacket.chunkZ = chunkZ;
		short[] blocksStateId = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z];
		for (int i = 0; i < blocksStateId.length; i++) {
			blocksStateId[i] = blockProvider.apply(ChunkUtils.getBlockPosition(i, 0, 0));
		}
		fullDataPacket.blocksStateId = blocksStateId;
		fullDataPacket.customBlocksId = new short[0];
		fullDataPacket.blockEntities = new CopyOnWriteArraySet<>(blockEntities);
		fullDataPacket.blocksData = new Int2ObjectOpenHashMap<>(blocksData);
		return fullDataPacket;
	}

}
