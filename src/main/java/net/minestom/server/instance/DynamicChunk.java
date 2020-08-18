package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.data.Data;
import net.minestom.server.data.SerializableData;
import net.minestom.server.entity.pathfinding.PFBlockDescription;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.UpdateConsumer;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.world.biomes.Biome;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

public class DynamicChunk extends Chunk {

    // blocks id based on coordinate, see Chunk#getBlockIndex
    private final short[] blocksStateId = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z];
    private final short[] customBlocksId = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z];

    public DynamicChunk(Biome[] biomes, int chunkX, int chunkZ) {
        super(biomes, chunkX, chunkZ);
    }

    @Override
    public void UNSAFE_removeCustomBlock(int x, int y, int z) {
        final int index = getBlockIndex(x, y, z);
        this.customBlocksId[index] = 0; // Set to none
        this.blocksData.remove(index);

        this.updatableBlocks.remove(index);
        this.updatableBlocksLastUpdate.remove(index);

        this.blockEntities.remove(index);
    }

    @Override
    protected void setBlock(int x, int y, int z, short blockStateId, short customId, Data data, UpdateConsumer updateConsumer) {

        {
            // Update pathfinder
            if (columnarSpace != null) {
                final ColumnarOcclusionFieldList columnarOcclusionFieldList = columnarSpace.occlusionFields();
                final PFBlockDescription blockDescription = PFBlockDescription.getBlockDescription(blockStateId);
                columnarOcclusionFieldList.onBlockChanged(x, y, z, blockDescription, 0);
            }
        }

        final int index = getBlockIndex(x, y, z);
        if (blockStateId != 0 || customId != 0 && updateConsumer != null) { // Allow custom air block for update purpose, refused if no update consumer has been found
            this.blocksStateId[index] = blockStateId;
            this.customBlocksId[index] = customId;
        } else {
            // Block has been deleted, clear cache and return

            this.blocksStateId[index] = 0; // Set to air

            this.blocksData.remove(index);

            this.updatableBlocks.remove(index);
            this.updatableBlocksLastUpdate.remove(index);

            this.blockEntities.remove(index);

            this.packetUpdated = false;
            return;
        }

        // Set the new data (or remove from the map if is null)
        if (data != null) {
            this.blocksData.put(index, data);
        } else {
            this.blocksData.remove(index);
        }

        // Set update consumer
        if (updateConsumer != null) {
            this.updatableBlocks.add(index);
            this.updatableBlocksLastUpdate.put(index, System.currentTimeMillis());
        } else {
            this.updatableBlocks.remove(index);
            this.updatableBlocksLastUpdate.remove(index);
        }

        // Set block entity
        if (isBlockEntity(blockStateId)) {
            this.blockEntities.add(index);
        } else {
            this.blockEntities.remove(index);
        }

        this.packetUpdated = false;
    }

    @Override
    public short getBlockStateId(int x, int y, int z) {
        final int index = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(index, 0, blocksStateId.length)) {
            return 0; // TODO: custom invalid block
        }
        return blocksStateId[index];
    }

    @Override
    public short getCustomBlockId(int x, int y, int z) {
        final int index = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(index, 0, blocksStateId.length)) {
            return 0; // TODO: custom invalid block
        }
        return customBlocksId[index];
    }

    @Override
    public CustomBlock getCustomBlock(int x, int y, int z) {
        final int index = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(index, 0, blocksStateId.length)) {
            return null; // TODO: custom invalid block
        }
        final short id = customBlocksId[index];
        return id != 0 ? BLOCK_MANAGER.getCustomBlock(id) : null;
    }

    @Override
    protected void refreshBlockValue(int x, int y, int z, short blockStateId, short customId) {
        final int blockIndex = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(blockIndex, 0, blocksStateId.length)) {
            return;
        }

        this.blocksStateId[blockIndex] = blockStateId;
        this.customBlocksId[blockIndex] = customId;
    }

    @Override
    protected void refreshBlockStateId(int x, int y, int z, short blockStateId) {
        final int blockIndex = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(blockIndex, 0, blocksStateId.length)) {
            return;
        }

        this.blocksStateId[blockIndex] = blockStateId;
    }

    @Override
    protected byte[] getSerializedData() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(output);

        for (int i = 0; i < BIOME_COUNT; i++) {
            dos.writeByte(biomes[i].getId());
        }

        for (byte x = 0; x < CHUNK_SIZE_X; x++) {
            for (short y = 0; y < CHUNK_SIZE_Y; y++) {
                for (byte z = 0; z < CHUNK_SIZE_Z; z++) {
                    final int index = getBlockIndex(x, y, z);

                    final short blockStateId = blocksStateId[index];
                    final short customBlockId = customBlocksId[index];

                    if (blockStateId == 0 && customBlockId == 0)
                        continue;

                    final Data data = blocksData.get(index);

                    // Chunk coordinates
                    dos.writeInt(x);
                    dos.writeInt(y);
                    dos.writeInt(z);

                    // Id
                    dos.writeShort(blockStateId);
                    dos.writeShort(customBlockId);

                    // Data
                    final boolean hasData = data instanceof SerializableData;
                    dos.writeBoolean(hasData);
                    if (hasData) {
                        final byte[] d = ((SerializableData) data).getSerializedData();
                        dos.writeInt(d.length);
                        dos.write(d);
                    }
                }
            }
        }

        return output.toByteArray();
    }

    @Override
    protected ChunkDataPacket getFreshPacket() {
        ChunkDataPacket fullDataPacket = new ChunkDataPacket();
        fullDataPacket.biomes = biomes.clone();
        fullDataPacket.chunkX = chunkX;
        fullDataPacket.chunkZ = chunkZ;
        fullDataPacket.blocksStateId = blocksStateId.clone();
        fullDataPacket.customBlocksId = customBlocksId.clone();
        fullDataPacket.blockEntities = new CopyOnWriteArraySet<>(blockEntities);
        fullDataPacket.blocksData = new Int2ObjectOpenHashMap<>(blocksData);
        return fullDataPacket;
    }

}
