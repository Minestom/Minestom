package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.SerializerUtils;
import fr.themode.minestom.utils.Utils;
import fr.themode.minestom.utils.buffer.BufferUtils;
import fr.themode.minestom.utils.buffer.BufferWrapper;
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

    private static final int CHUNK_SECTION_COUNT = 16;
    private static final int BITS_PER_ENTRY = 14;
    private static final int MAX_BUFFER_SIZE = (Short.BYTES + Byte.BYTES + 5 * Byte.BYTES + (4096 * BITS_PER_ENTRY / Long.SIZE * Long.BYTES)) * CHUNK_SECTION_COUNT + 256 * Integer.BYTES;

    @Override
    public void write(PacketWriter writer) {
        writer.writeInt(chunk.getChunkX());
        writer.writeInt(chunk.getChunkZ());
        writer.writeBoolean(fullChunk);

        int mask = 0;
        BufferWrapper blocks = BufferUtils.getBuffer(MAX_BUFFER_SIZE);
        for (int i = 0; i < CHUNK_SECTION_COUNT; i++) {
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
            for (int z = 0; z < 1024; z++) {
                // TODO proper chunk section biome
                writer.writeInt(chunk.getBiome().getId());
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

    private short[] getSection(Chunk chunk, int section) {
        short[] blocks = new short[16 * 16 * 16];
        boolean empty = true;

        for (byte y = 0; y < 16; y++) {
            for (byte x = 0; x < 16; x++) {
                for (byte z = 0; z < 16; z++) {
                    short blockId = chunk.getBlockId(x, (byte) (y + 16 * section), z);
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
