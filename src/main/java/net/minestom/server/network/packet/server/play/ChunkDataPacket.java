package net.minestom.server.network.packet.server.play;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.palette.PaletteStorage;
import net.minestom.server.instance.palette.Section;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.BufUtils;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.cache.CacheablePacket;
import net.minestom.server.utils.cache.TemporaryCache;
import net.minestom.server.utils.cache.TimedBuffer;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ChunkDataPacket implements ServerPacket, CacheablePacket {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();
    private static final TemporaryCache<TimedBuffer> CACHE = new TemporaryCache<>(5, TimeUnit.MINUTES,
            notification -> notification.getValue().getBuffer().release());

    public boolean fullChunk;
    public Biome[] biomes;
    public int chunkX, chunkZ;

    public PaletteStorage paletteStorage;
    public PaletteStorage customBlockPaletteStorage;

    public IntSet blockEntities;
    public Int2ObjectMap<Data> blocksData;

    public int[] sections = new int[0];

    private static final byte CHUNK_SECTION_COUNT = 16;
    private static final int MAX_BITS_PER_ENTRY = 16;
    private static final int MAX_BUFFER_SIZE = (Short.BYTES + Byte.BYTES + 5 * Byte.BYTES + (4096 * MAX_BITS_PER_ENTRY / Long.SIZE * Long.BYTES)) * CHUNK_SECTION_COUNT + 256 * Integer.BYTES;

    // Cacheable data
    private final UUID identifier;
    private final long timestamp;

    /**
     * Block entities NBT, as read from raw packet data.
     * Only filled by #read, and unused at the moment.
     */
    public NBTCompound[] blockEntitiesNBT = new NBTCompound[0];
    /**
     * Heightmaps NBT, as read from raw packet data.
     * Only filled by #read, and unused at the moment.
     */
    public NBTCompound heightmapsNBT;

    private ChunkDataPacket() {
        this(new UUID(0, 0), 0);
    }

    public ChunkDataPacket(@Nullable UUID identifier, long timestamp) {
        this.identifier = identifier;
        this.timestamp = timestamp;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(chunkX);
        writer.writeInt(chunkZ);
        writer.writeBoolean(fullChunk);

        int mask = 0;
        ByteBuf blocks = BufUtils.getBuffer(MAX_BUFFER_SIZE);
        for (byte i = 0; i < CHUNK_SECTION_COUNT; i++) {
            if (fullChunk || (sections.length == CHUNK_SECTION_COUNT && sections[i] != 0)) {
                final Section section = paletteStorage.getSections()[i];
                if (section == null) {
                    // Section not loaded
                    continue;
                }
                if (section.getBlocks().length > 0) { // section contains at least one block
                    mask |= 1 << i;
                    Utils.writeSectionBlocks(blocks, section);
                }
            }
        }

        writer.writeVarInt(mask);

        // TODO: don't hardcode heightmaps
        // Heightmap
        int[] motionBlocking = new int[16 * 16];
        int[] worldSurface = new int[16 * 16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                motionBlocking[x + z * 16] = 4;
                worldSurface[x + z * 16] = 5;
            }
        }

        {
            writer.writeNBT("",
                    new NBTCompound()
                            .setLongArray("MOTION_BLOCKING", Utils.encodeBlocks(motionBlocking, 9))
                            .setLongArray("WORLD_SURFACE", Utils.encodeBlocks(worldSurface, 9))
            );
        }

        // Biome data
        if (fullChunk) {
            writer.writeVarInt(biomes.length);
            for (Biome biome : biomes) {
                writer.writeVarInt(biome.getId());
            }
        }

        // Data
        writer.writeVarInt(blocks.writerIndex());
        writer.getBuffer().writeBytes(blocks);
        blocks.release();

        // Block entities
        if (blockEntities == null) {
            writer.writeVarInt(0);
        } else {
            writer.writeVarInt(blockEntities.size());

            for (int index : blockEntities) {
                final BlockPosition blockPosition = ChunkUtils.getBlockPosition(index, chunkX, chunkZ);

                NBTCompound nbt = new NBTCompound()
                        .setInt("x", blockPosition.getX())
                        .setInt("y", blockPosition.getY())
                        .setInt("z", blockPosition.getZ());

                if (customBlockPaletteStorage != null) {
                    final short customBlockId = customBlockPaletteStorage.getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                    final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
                    if (customBlock != null) {
                        final Data data = blocksData.get(index);
                        customBlock.writeBlockEntity(blockPosition, data, nbt);
                    }
                }
                writer.writeNBT("", nbt);
            }
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        chunkX = reader.readInt();
        chunkZ = reader.readInt();
        fullChunk = reader.readBoolean();

        int mask = reader.readVarInt();
        try {
            // TODO: Use heightmaps
            // unused at the moment
            heightmapsNBT = (NBTCompound) reader.readTag();

            // Biomes
            if (fullChunk) {
                int[] biomesIds = reader.readVarIntArray();
                this.biomes = new Biome[biomesIds.length];
                for (int i = 0; i < biomesIds.length; i++) {
                    this.biomes[i] = MinecraftServer.getBiomeManager().getById(biomesIds[i]);
                }
            }

            // Data
            this.paletteStorage = new PaletteStorage(8, 1);
            int blockArrayLength = reader.readVarInt();
            for (int section = 0; section < CHUNK_SECTION_COUNT; section++) {
                boolean hasSection = (mask & 1 << section) != 0;
                if (!hasSection)
                    continue;
                short blockCount = reader.readShort();
                byte bitsPerEntry = reader.readByte();

                // Resize palette if necessary
                if (bitsPerEntry > paletteStorage.getSections()[section].getBitsPerEntry()) {
                    paletteStorage.getSections()[section].resize(bitsPerEntry);
                }

                // Retrieve palette values
                if (bitsPerEntry < 9) {
                    int paletteSize = reader.readVarInt();
                    for (int i = 0; i < paletteSize; i++) {
                        final int paletteValue = reader.readVarInt();
                        paletteStorage.getSections()[section].getPaletteBlockMap().put((short) i, (short) paletteValue);
                        paletteStorage.getSections()[section].getBlockPaletteMap().put((short) paletteValue, (short) i);
                    }
                }

                // Read blocks
                int dataLength = reader.readVarInt();
                long[] data = paletteStorage.getSections()[section].getBlocks();
                for (int i = 0; i < dataLength; i++) {
                    data[i] = reader.readLong();
                }
            }

            // Block entities
            int blockEntityCount = reader.readVarInt();
            blockEntities = new IntOpenHashSet();
            blockEntitiesNBT = new NBTCompound[blockEntityCount];
            for (int i = 0; i < blockEntityCount; i++) {
                NBTCompound tag = (NBTCompound) reader.readTag();
                blockEntitiesNBT[i] = tag;
            }
        } catch (IOException | NBTException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            // TODO: should we throw to avoid an invalid packet?
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHUNK_DATA;
    }

    @NotNull
    @Override
    public TemporaryCache<TimedBuffer> getCache() {
        return CACHE;
    }

    @Override
    public UUID getIdentifier() {
        return identifier;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}