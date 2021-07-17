package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.SerializableData;
import net.minestom.server.data.SerializableDataImpl;
import net.minestom.server.entity.pathfinding.PFBlockDescription;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.palette.PaletteStorage;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.block.CustomBlockUtils;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.Set;

/**
 * Represents a {@link Chunk} which store each individual block in memory.
 * <p>
 * WARNING: not thread-safe.
 */
public class DynamicChunk extends Chunk {

    /**
     * Represents the version which will be present in the serialized output.
     * Used to define which deserializer to use.
     */
    private static final int DATA_FORMAT_VERSION = 1;

    // WARNING: not thread-safe and should not be changed
    protected PaletteStorage blockPalette;
    protected PaletteStorage customBlockPalette;

    // Used to get all blocks with data (no null)
    // Key is still chunk coordinates (see #getBlockIndex)
    protected final Int2ObjectOpenHashMap<Data> blocksData = new Int2ObjectOpenHashMap<>();

    // Contains CustomBlocks' block index which are updatable
    protected final IntOpenHashSet updatableBlocks = new IntOpenHashSet();
    // (block index)/(last update in ms)
    protected final Int2LongMap updatableBlocksLastUpdate = new Int2LongOpenHashMap();

    // Block entities
    protected final IntOpenHashSet blockEntities = new IntOpenHashSet();

    private long lastChangeTime;

    private SoftReference<ChunkDataPacket> cachedPacket = new SoftReference<>(null);
    private long cachedPacketTime;

    public DynamicChunk(@NotNull Instance instance, @Nullable Biome[] biomes, int chunkX, int chunkZ,
                        @NotNull PaletteStorage blockPalette, @NotNull PaletteStorage customBlockPalette) {
        super(instance, biomes, chunkX, chunkZ, true);
        this.blockPalette = blockPalette;
        this.customBlockPalette = customBlockPalette;
    }

    public DynamicChunk(@NotNull Instance instance, @Nullable Biome[] biomes, int chunkX, int chunkZ) {
        this(instance, biomes, chunkX, chunkZ,
                new PaletteStorage(8, 2),
                new PaletteStorage(8, 2));
    }

    @Override
    public void UNSAFE_setBlock(int x, int y, int z, short blockStateId, short customBlockId, Data data, boolean updatable) {

        {
            // Update pathfinder
            if (columnarSpace != null) {
                final ColumnarOcclusionFieldList columnarOcclusionFieldList = columnarSpace.occlusionFields();
                final PFBlockDescription blockDescription = PFBlockDescription.getBlockDescription(blockStateId);
                columnarOcclusionFieldList.onBlockChanged(x, y, z, blockDescription, 0);
            }
        }

        final int index = getBlockIndex(x, y, z);
        // True if the block is not complete air without any custom block capabilities
        final boolean hasBlock = blockStateId != 0 || customBlockId != 0;

        setBlockAt(blockPalette, x, y, z, blockStateId);
        setBlockAt(customBlockPalette, x, y, z, customBlockId);

        if (!hasBlock) {
            // Block has been deleted, clear cache and return

            this.blocksData.remove(index);

            this.updatableBlocks.remove(index);
            this.updatableBlocksLastUpdate.remove(index);

            this.blockEntities.remove(index);
            return;
        }

        // Set the new data (or remove from the map if is null)
        if (data != null) {
            this.blocksData.put(index, data);
        } else {
            this.blocksData.remove(index);
        }

        // Set update consumer
        if (updatable) {
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
    }

    @Override
    public void tick(long time) {
        if (updatableBlocks.isEmpty())
            return;

        // Block all chunk operation during the update
        final IntIterator iterator = new IntOpenHashSet(updatableBlocks).iterator();
        while (iterator.hasNext()) {
            final int index = iterator.nextInt();
            final CustomBlock customBlock = getCustomBlock(index);

            // Update cooldown
            final Duration updateFrequency = customBlock.getUpdateFrequency();
            if (updateFrequency != null) {
                final long lastUpdate = updatableBlocksLastUpdate.get(index);
                final boolean hasCooldown = Cooldown.hasCooldown(time, lastUpdate, updateFrequency);
                if (hasCooldown)
                    continue;

                this.updatableBlocksLastUpdate.put(index, time); // Refresh last update time

                final BlockPosition blockPosition = ChunkUtils.getBlockPosition(index, chunkX, chunkZ);
                final Data data = getBlockData(index);
                customBlock.update(instance, blockPosition, data);
            }
        }
    }

    @Override
    public short getBlockStateId(int x, int y, int z) {
        return getBlockAt(blockPalette, x, y, z);
    }

    @Override
    public short getCustomBlockId(int x, int y, int z) {
        return getBlockAt(customBlockPalette, x, y, z);
    }

    @Override
    protected void refreshBlockValue(int x, int y, int z, short blockStateId, short customBlockId) {
        setBlockAt(blockPalette, x, y, z, blockStateId);
        setBlockAt(customBlockPalette, x, y, z, customBlockId);
    }

    @Override
    protected void refreshBlockStateId(int x, int y, int z, short blockStateId) {
        setBlockAt(blockPalette, x, y, z, blockStateId);
    }

    @Override
    public Data getBlockData(int index) {
        return blocksData.get(index);
    }

    @Override
    public void setBlockData(int x, int y, int z, Data data) {
        final int index = getBlockIndex(x, y, z);
        if (data != null) {
            this.blocksData.put(index, data);
        } else {
            this.blocksData.remove(index);
        }
    }

    @NotNull
    @Override
    public Set<Integer> getBlockEntities() {
        return blockEntities;
    }

    @Override
    public long getLastChangeTime() {
        return lastChangeTime;
    }

    /**
     * Serialize this {@link Chunk} based on {@link #readChunk(BinaryReader, ChunkCallback)}
     * <p>
     * It is also used by the default {@link IChunkLoader} which is {@link MinestomBasicChunkLoader}
     *
     * @return the serialized chunk data
     */
    @Override
    public byte[] getSerializedData() {

        // Used for blocks data (unused if empty at the end)
        Object2ShortMap<String> typeToIndexMap = new Object2ShortOpenHashMap<>();

        // Order of buffers in the final serialized array
        BinaryWriter versionWriter = new BinaryWriter();
        BinaryWriter dataIndexWriter = new BinaryWriter();
        BinaryWriter chunkWriter = new BinaryWriter();

        // VERSION WRITER
        {
            versionWriter.writeInt(DATA_FORMAT_VERSION);
            versionWriter.writeInt(MinecraftServer.PROTOCOL_VERSION);
        }

        // CHUNK DATA WRITER
        {
            // Chunk data
            final boolean hasChunkData = data instanceof SerializableData && !data.isEmpty();
            chunkWriter.writeBoolean(hasChunkData);
            if (hasChunkData) {
                // Get the un-indexed data
                final byte[] serializedData = ((SerializableData) data).getSerializedData(typeToIndexMap, false);
                chunkWriter.writeBytes(serializedData);
            }

            // Write the biomes id
            for (int i = 0; i < 1024; i++) { // TODO variable biome count
                final byte id = (byte) biomes[i].getId();
                chunkWriter.writeByte(id);
            }

            // Loop all blocks
            for (byte x = 0; x < CHUNK_SIZE_X; x++) {
                for (short y = 0; y < 256; y++) { // TODO increase max size
                    for (byte z = 0; z < CHUNK_SIZE_Z; z++) {
                        final int index = getBlockIndex(x, y, z);

                        final short blockStateId = getBlockAt(blockPalette, x, y, z);
                        final short customBlockId = getBlockAt(customBlockPalette, x, y, z);

                        // No block at the position
                        if (blockStateId == 0 && customBlockId == 0)
                            continue;

                        // Chunk coordinates
                        chunkWriter.writeShort((short) index);

                        // Block ids
                        chunkWriter.writeShort(blockStateId);
                        chunkWriter.writeShort(customBlockId);

                        // Data
                        final Data data = getBlockData(index);
                        final boolean hasBlockData = data instanceof SerializableData && !data.isEmpty();
                        chunkWriter.writeBoolean(hasBlockData);
                        if (hasBlockData) {
                            // Get the un-indexed data
                            final byte[] serializedData = ((SerializableData) data).getSerializedData(typeToIndexMap, false);
                            chunkWriter.writeBytes(serializedData);
                        }
                    }
                }
            }
        }

        // DATA INDEX WRITER
        {
            // If the chunk data contains SerializableData type, it needs to be added in the header
            final boolean hasDataIndex = !typeToIndexMap.isEmpty();
            dataIndexWriter.writeBoolean(hasDataIndex);
            if (hasDataIndex) {
                // Get the index buffer (prefixed by true to say that the chunk contains data indexes)
                SerializableData.writeDataIndexHeader(dataIndexWriter, typeToIndexMap);
            }
        }

        final BinaryWriter finalBuffer = new BinaryWriter(
                versionWriter.getBuffer(),
                dataIndexWriter.getBuffer(),
                chunkWriter.getBuffer());

        return finalBuffer.toByteArray();
    }

    @Override
    public void readChunk(@NotNull BinaryReader reader, ChunkCallback callback) {
        // Check the buffer length
        final int length = reader.available();
        Check.argCondition(length == 0, "The length of the buffer must be > 0");

        // Run in the scheduler thread pool
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            synchronized (this) {

                // Track changes in the buffer
                {
                    final boolean changed = reader.available() != length;
                    Check.stateCondition(changed,
                            "The number of readable bytes changed, be sure to do not manipulate the buffer until the end of the reading.");
                }

                // VERSION DATA
                final int dataFormatVersion = reader.readInteger();
                final int dataProtocol = reader.readInteger();

                if (dataFormatVersion != DATA_FORMAT_VERSION) {
                    throw new UnsupportedOperationException(
                            "You are parsing an old version of the chunk format, please contact the developer: " + dataFormatVersion);
                }

                // INDEX DATA
                // Used for blocks data
                Object2ShortMap<String> typeToIndexMap = null;

                // Get if the chunk has data indexes (used for blocks data)
                final boolean hasDataIndex = reader.readBoolean();
                if (hasDataIndex) {
                    // Get the data indexes which will be used to read all the individual data
                    typeToIndexMap = SerializableData.readDataIndexes(reader);
                }

                // CHUNK DATA
                // Chunk data
                final boolean hasChunkData = reader.readBoolean();
                if (hasDataIndex && hasChunkData) {
                    SerializableData serializableData = new SerializableDataImpl();
                    serializableData.readSerializedData(reader, typeToIndexMap);
                }

                // Biomes
                for (int i = 0; i < 1024; i++) { // TODO variable biome count
                    final byte id = reader.readByte();
                    this.biomes[i] = BIOME_MANAGER.getById(id);
                }

                // Loop for all blocks in the chunk
                while (reader.available() > 0) {
                    // Position
                    final short index = reader.readShort();
                    final byte x = ChunkUtils.blockIndexToChunkPositionX(index);
                    final short y = ChunkUtils.blockIndexToChunkPositionY(index);
                    final byte z = ChunkUtils.blockIndexToChunkPositionZ(index);

                    // Block type
                    final short blockStateId = reader.readShort();
                    final short customBlockId = reader.readShort();

                    // Data
                    SerializableData data = null;
                    {
                        final boolean hasBlockData = reader.readBoolean();
                        // Data deserializer
                        if (hasDataIndex && hasBlockData) {
                            // Read the data with the deserialized index map
                            data = new SerializableDataImpl();
                            data.readSerializedData(reader, typeToIndexMap);
                        }
                    }

                    UNSAFE_setBlock(x, y, z, blockStateId, customBlockId, data, CustomBlockUtils.hasUpdate(customBlockId));
                }

                // Finished reading
                OptionalCallback.execute(callback, this);
            }
        }).schedule();
    }

    @NotNull
    @Override
    public ChunkDataPacket createChunkPacket() {
        ChunkDataPacket packet = cachedPacket.get();
        if (packet != null && cachedPacketTime == getLastChangeTime()) {
            return packet;
        }
        packet = new ChunkDataPacket(getIdentifier(), getLastChangeTime());
        packet.biomes = biomes;
        packet.chunkX = chunkX;
        packet.chunkZ = chunkZ;
        packet.paletteStorage = blockPalette.clone();
        packet.customBlockPaletteStorage = customBlockPalette.clone();
        packet.blockEntities = blockEntities.clone();
        packet.blocksData = blocksData.clone();

        this.cachedPacketTime = getLastChangeTime();
        this.cachedPacket = new SoftReference<>(packet);
        return packet;
    }

    @NotNull
    @Override
    public Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
        DynamicChunk dynamicChunk = new DynamicChunk(instance, biomes.clone(), chunkX, chunkZ);
        dynamicChunk.blockPalette = blockPalette.clone();
        dynamicChunk.customBlockPalette = customBlockPalette.clone();
        dynamicChunk.blocksData.putAll(blocksData);
        dynamicChunk.updatableBlocks.addAll(updatableBlocks);
        dynamicChunk.updatableBlocksLastUpdate.putAll(updatableBlocksLastUpdate);
        dynamicChunk.blockEntities.addAll(blockEntities);

        return dynamicChunk;
    }

    @Override
    public void reset() {
        this.blockPalette.clear();
        this.customBlockPalette.clear();

        this.blocksData.clear();
        this.updatableBlocks.clear();
        this.updatableBlocksLastUpdate.clear();
        this.blockEntities.clear();
    }

    private short getBlockAt(@NotNull PaletteStorage paletteStorage, int x, int y, int z) {
        return paletteStorage.getBlockAt(x, y, z);
    }

    private void setBlockAt(@NotNull PaletteStorage paletteStorage, int x, int y, int z, short blockId) {
        paletteStorage.setBlockAt(x, y, z, blockId);
        this.lastChangeTime = System.currentTimeMillis();
    }
}