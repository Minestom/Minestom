package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.SerializerUtils;
import fr.themode.minestom.utils.Utils;
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

    @Override
    public void write(PacketWriter writer) {
        writer.writeInt(chunk.getChunkX());
        writer.writeInt(chunk.getChunkZ());
        writer.writeBoolean(fullChunk);


        int mask = 0;
        Buffer blocks = Buffer.create();
        for (int i = 0; i < 16; i++) {
            if (fullChunk || (sections.length == 16 && sections[i] != 0)) {
                mask |= 1 << i;
                short[] section = getSection(chunk, i);
                Utils.writeBlocks(blocks, section, 14);
            }
        }
        // Biome data
        if (fullChunk) {
            int[] biomeData = new int[256];
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    biomeData[z * 16 | x] = chunk.getBiome().getId();
                }
            }
            for (int i = 0; i < biomeData.length; i++) {
                blocks.putInt(biomeData[i]);
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

        writer.writeVarInt(blocks.length());
        writer.writeBytes(blocks.getAllBytes());

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
        for (byte y = 0; y < 16; y++) {
            for (byte x = 0; x < 16; x++) {
                for (byte z = 0; z < 16; z++) {
                    int index = (((y * 16) + x) * 16) + z;
                    blocks[index] = chunk.getBlockId(x, (byte) (y + 16 * section), z);
                }
            }
        }
        return blocks;
    }

    @Override
    public int getId() {
        return 0x21;
    }
}
