package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.SerializableData;
import net.minestom.server.data.SerializableDataImpl;
import net.minestom.server.entity.pathfinding.PFBlockDescription;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.lang.ref.SoftReference;
import java.util.Map;
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

    protected final Int2ObjectRBTreeMap<Section> sectionMap = new Int2ObjectRBTreeMap<>();

    // Key = ChunkUtils#getBlockIndex
    protected final Int2ObjectOpenHashMap<BlockHandler> handlerMap = new Int2ObjectOpenHashMap<>();
    protected final Int2ObjectOpenHashMap<NBTCompound> nbtMap = new Int2ObjectOpenHashMap<>();

    private long lastChangeTime;

    private SoftReference<ChunkDataPacket> cachedPacket = new SoftReference<>(null);
    private long cachedPacketTime;

    public DynamicChunk(@NotNull Instance instance, @Nullable Biome[] biomes, int chunkX, int chunkZ) {
        super(instance, biomes, chunkX, chunkZ, true);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        final short blockStateId = block.getStateId();
        final BlockHandler handler = block.getHandler();
        final NBTCompound nbt = null; // TODO
        final boolean updatable = false; // TODO
        {
            // Update pathfinder
            if (columnarSpace != null) {
                final ColumnarOcclusionFieldList columnarOcclusionFieldList = columnarSpace.occlusionFields();
                final PFBlockDescription blockDescription = PFBlockDescription.getBlockDescription(block);
                columnarOcclusionFieldList.onBlockChanged(x, y, z, blockDescription, 0);
            }
        }

        this.lastChangeTime = System.currentTimeMillis();
        {
            Section section = retrieveSection(y);
            section.setBlockAt(x, y, z, blockStateId);
        }

        final int index = getBlockIndex(x, y, z);
        // Handler
        if (handler != null) {
            this.handlerMap.put(index, handler);
        } else {
            this.handlerMap.remove(index);
        }
        // Nbt
        if (nbt != null) {
            this.nbtMap.put(index, nbt);
        } else {
            this.nbtMap.remove(index);
        }
    }

    @Override
    public @NotNull Map<Integer, Section> getSections() {
        return sectionMap;
    }

    @Override
    public @Nullable Section getSection(int section) {
        return sectionMap.get(section);
    }

    @Override
    public void tick(long time) {
        // TODO block update
    }

    @Override
    public @NotNull Block getBlock(int x, int y, int z) {
        final Section section = retrieveSection(y);
        final int index = ChunkUtils.getBlockIndex(x, y, z);
        final short blockStateId = section.getBlockAt(x, y, z);
        BlockHandler handler = handlerMap.get(index);
        NBTCompound nbt = nbtMap.get(index);
        Block block = Block.fromStateId(blockStateId);
        if (block == null) {
            return Block.AIR;
        }
        return block
                .withHandler(handler)
                .withNbt(nbt);
    }

    @NotNull
    @Override
    public Set<Integer> getBlockEntities() {
        return nbtMap.keySet();
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

                        final Section section = retrieveSection(y);

                        final short blockStateId = section.getBlockAt(x, y, z);
                        final short customBlockId = 0;//getBlockAt(customBlockPalette, x, y, z);

                        // No block at the position
                        if (blockStateId == 0 && customBlockId == 0)
                            continue;

                        // Chunk coordinates
                        chunkWriter.writeShort((short) index);

                        // Block ids
                        chunkWriter.writeShort(blockStateId);
                        chunkWriter.writeShort(customBlockId);

                        // Data
                        final Data data = null;//getBlockData(index);
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

                    Block block = Block.fromStateId(blockStateId);
                    // TODO read other data

                    setBlock(x, y, z, block);
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
        packet.sections = sectionMap.clone(); // TODO deep clone
        packet.handlerMap = handlerMap.clone();
        packet.nbtMap = nbtMap.clone();

        this.cachedPacketTime = getLastChangeTime();
        this.cachedPacket = new SoftReference<>(packet);
        return packet;
    }

    @NotNull
    @Override
    public Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ) {
        DynamicChunk dynamicChunk = new DynamicChunk(instance, biomes.clone(), chunkX, chunkZ);
        for (var entry : sectionMap.int2ObjectEntrySet()) {
            dynamicChunk.sectionMap.put(entry.getIntKey(), entry.getValue().clone());
        }
        dynamicChunk.handlerMap.putAll(handlerMap);
        dynamicChunk.nbtMap.putAll(nbtMap);

        return dynamicChunk;
    }

    @Override
    public void reset() {
        this.sectionMap.values().forEach(Section::clear);
        this.handlerMap.clear();
        this.nbtMap.clear();
    }

    private @NotNull Section retrieveSection(int y) {
        final int sectionIndex = ChunkUtils.getSectionAt(y);
        return sectionMap.computeIfAbsent(sectionIndex, key -> new Section());
    }
}
