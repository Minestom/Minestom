package net.minestom.server.network.packet.server.play;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Biome;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.buffer.BufferUtils;
import net.minestom.server.utils.buffer.BufferWrapper;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.DoubleTag;
import net.querz.nbt.LongArrayTag;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

public class ChunkDataPacket implements ServerPacket {

    public boolean fullChunk;
    public Chunk chunk;
    public int[] sections;

    private static final byte CHUNK_SECTION_COUNT = 16;
    private static final int BITS_PER_ENTRY = 14;
    private static final int MAX_BUFFER_SIZE = (Short.BYTES + Byte.BYTES + 5 * Byte.BYTES + (4096 * BITS_PER_ENTRY / Long.SIZE * Long.BYTES)) * CHUNK_SECTION_COUNT + 256 * Integer.BYTES;

    @Override
    public void write(PacketWriter writer) {
        writer.writeInt(chunk.getChunkX());
        writer.writeInt(chunk.getChunkZ());
        writer.writeBoolean(fullChunk);

        int mask = 0;
        BufferWrapper blocks = BufferUtils.getBuffer(MAX_BUFFER_SIZE);
        for (byte i = 0; i < CHUNK_SECTION_COUNT; i++) {
            if (fullChunk || (sections.length == CHUNK_SECTION_COUNT && sections[i] != 0)) {
                short[] section = getSection(chunk, i);
                if (section != null) { // section contains at least one block
                    mask |= 1 << i;
                    Utils.writeBlocks(blocks, section, BITS_PER_ENTRY);
                } else {
                    mask |= 0 << i;
                }
            } else {
                mask |= 0 << i;
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
            CompoundTag compound = new CompoundTag();
            compound.put("MOTION_BLOCKING", new LongArrayTag(Utils.encodeBlocks(motionBlocking, 9)));
            compound.put("WORLD_SURFACE", new LongArrayTag(Utils.encodeBlocks(worldSurface, 9)));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                compound.serialize(new DataOutputStream(outputStream), 100);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] data = outputStream.toByteArray();
            writer.writeBytes(data);
        }

        // Biome data
        if (fullChunk) {
            Biome[] biomes = chunk.getBiomes();
            for (int i = 0; i < biomes.length; i++) {
                writer.writeInt(biomes[i].getId());
            }
        }

        // Data
        writer.writeVarInt(blocks.getSize());
        writer.writeBufferAndFree(blocks);

        // Block entities
        Set<Integer> blockEntities = chunk.getBlockEntities();
        writer.writeVarInt(blockEntities.size());

        for (Integer index : blockEntities) {
            BlockPosition blockPosition = SerializerUtils.indexToChunkBlockPosition(index);
            CompoundTag blockEntity = new CompoundTag();
            blockEntity.put("x", new DoubleTag(blockPosition.getX() + 16 * chunk.getChunkX()));
            blockEntity.put("y", new DoubleTag(blockPosition.getY()));
            blockEntity.put("z", new DoubleTag(blockPosition.getZ() + 16 * chunk.getChunkZ()));
            CustomBlock customBlock = chunk.getCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            if (customBlock != null) {
                Data data = chunk.getData(blockPosition.getX(), (byte)blockPosition.getY(), blockPosition.getZ());
                customBlock.writeBlockEntity(blockPosition, data, blockEntity);
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                blockEntity.serialize(new DataOutputStream(os), 100);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] d = os.toByteArray();
            writer.writeBytes(d);
        }
    }

    private short[] getSection(Chunk chunk, byte section) {
        short[] blocks = new short[Chunk.CHUNK_SIZE_X * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SIZE_Z];
        boolean empty = true;
        for (byte y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
            for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    short blockId = chunk.getBlockId(x, (y + Chunk.CHUNK_SECTION_SIZE * section), z);
                    if (blockId != 0)
                        empty = false;

                    int index = (((y * 16) + x) * 16) + z;
                    blocks[index] = blockId;
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