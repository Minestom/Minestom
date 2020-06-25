package net.minestom.server.network.packet.server.play;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.instance.Biome;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.buffer.BufferUtils;
import net.minestom.server.utils.buffer.BufferWrapper;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.nbt.NbtWriter;

import java.util.Set;

public class ChunkDataPacket implements ServerPacket {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    public boolean fullChunk;
    //todo make a changeable
    public boolean ignoreOldLighting = true;
    public Biome[] biomes;
    public int chunkX, chunkZ;

    public short[] blocksId;
    public short[] customBlocksId;

    public Set<Integer> blockEntities;
    public Int2ObjectMap<Data> blocksData;
    //public Chunk chunk;

    public int[] sections;

    private static final byte CHUNK_SECTION_COUNT = 16;
    private static final int BITS_PER_ENTRY = 15;
    private static final int MAX_BUFFER_SIZE = (Short.BYTES + Byte.BYTES + 5 * Byte.BYTES + (4096 * BITS_PER_ENTRY / Long.SIZE * Long.BYTES)) * CHUNK_SECTION_COUNT + 256 * Integer.BYTES;

    @Override
    public void write(PacketWriter writer) {
        NbtWriter nbtWriter = new NbtWriter(writer);

        writer.writeInt(chunkX);
        writer.writeInt(chunkZ);
        writer.writeBoolean(fullChunk);

        writer.writeBoolean(ignoreOldLighting);

        int mask = 0;
        BufferWrapper blocks = BufferUtils.getBuffer(MAX_BUFFER_SIZE);
        for (byte i = 0; i < CHUNK_SECTION_COUNT; i++) {
            if (fullChunk || (sections.length == CHUNK_SECTION_COUNT && sections[i] != 0)) {
                short[] section = getSection(i);
                if (section != null) { // section contains at least one block
                    mask |= 1 << i;
                    Utils.writeBlocks(blocks, section, BITS_PER_ENTRY);
                } else {
                    mask |= 0;
                }
            } else {
                mask |= 0;
            }
        }

        writer.writeVarInt(mask);

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
            nbtWriter.writeCompound("", compound -> {
                compound.writeLongArray("MOTION_BLOCKING", Utils.encodeBlocks(motionBlocking, 9));
                compound.writeLongArray("WORLD_SURFACE", Utils.encodeBlocks(worldSurface, 9));
            });
        }

        // Biome data
        if (fullChunk) {
            for (int i = 0; i < biomes.length; i++) {
                writer.writeInt(biomes[i].getId());
            }
        }

        // Data
        writer.writeVarInt(blocks.getSize());
        writer.writeBufferAndFree(blocks);

        // Block entities
        writer.writeVarInt(blockEntities.size());

        for (int index : blockEntities) {
            final BlockPosition blockPosition = ChunkUtils.getBlockPosition(index, chunkX, chunkZ);

            nbtWriter.writeCompound("", compound -> {
                compound.writeDouble("x", blockPosition.getX());
                compound.writeDouble("y", blockPosition.getY());
                compound.writeDouble("z", blockPosition.getZ());

                final short customBlockId = customBlocksId[index];
                final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
                if (customBlock != null) {
                    Data data = blocksData.get(index);
                    customBlock.writeBlockEntity(blockPosition, data, compound);
                }
            });
        }
    }

    private short[] getSection(byte section) {
        short[] blocks = new short[Chunk.CHUNK_SIZE_X * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SIZE_Z];
        boolean empty = true;
        for (byte y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
            for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    int yPos = (y + Chunk.CHUNK_SECTION_SIZE * section);
                    int index = ChunkUtils.getBlockIndex(x, yPos, z);
                    short blockId = blocksId[index];
                    if (blockId != 0)
                        empty = false;

                    int packetIndex = (((y * 16) + x) * 16) + z;
                    blocks[packetIndex] = blockId;
                }
            }
        }
        return empty ? null : blocks;
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHUNK_DATA;
    }
}