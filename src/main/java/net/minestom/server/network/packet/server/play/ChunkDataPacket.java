package net.minestom.server.network.packet.server.play;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Set;

public class ChunkDataPacket implements ServerPacket {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    public boolean fullChunk;
    public Biome[] biomes;
    public int chunkX, chunkZ;

    public short[] blocksStateId;
    public short[] customBlocksId;

    public Set<Integer> blockEntities;
    public Int2ObjectMap<Data> blocksData;
    //public Chunk chunk;

    public int[] sections;

    private static final byte CHUNK_SECTION_COUNT = 16;
    private static final int BITS_PER_ENTRY = 15;
    private static final int MAX_BUFFER_SIZE = (Short.BYTES + Byte.BYTES + 5 * Byte.BYTES + (4096 * BITS_PER_ENTRY / Long.SIZE * Long.BYTES)) * CHUNK_SECTION_COUNT + 256 * Integer.BYTES;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeInt(chunkX);
        writer.writeInt(chunkZ);
        writer.writeBoolean(fullChunk);

        int mask = 0;
        ByteBuf blocks = Unpooled.buffer(MAX_BUFFER_SIZE);
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

        // Block entities
        writer.writeVarInt(blockEntities.size());

        for (int index : blockEntities) {
            final BlockPosition blockPosition = ChunkUtils.getBlockPosition(index, chunkX, chunkZ);

            NBTCompound nbt = new NBTCompound()
                    .setDouble("x", blockPosition.getX())
                    .setDouble("y", blockPosition.getY())
                    .setDouble("z", blockPosition.getZ());

            final short customBlockId = customBlocksId[index];
            final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
            if (customBlock != null) {
                final Data data = blocksData.get(index);
                customBlock.writeBlockEntity(blockPosition, data, nbt);
            }
            writer.writeNBT("", nbt);
        }
    }

    private short[] getSection(byte section) {
        short[] blocks = new short[Chunk.CHUNK_SIZE_X * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SIZE_Z];
        boolean empty = true;
        for (byte y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
            for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    final int yPos = (y + Chunk.CHUNK_SECTION_SIZE * section);
                    final int index = ChunkUtils.getBlockIndex(x, yPos, z);
                    final short blockStateId = blocksStateId[index];
                    if (blockStateId != 0)
                        empty = false;

                    final int packetIndex = (((y * 16) + x) * 16) + z;
                    blocks[packetIndex] = blockStateId;
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