package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.LongArrayTag;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChunkDataPacket implements ServerPacket {

    public boolean fullChunk;
    public Chunk chunk;
    // TODO nbt tile entities

    @Override
    public void write(Buffer buffer) {
        buffer.putInt(chunk.getChunkX());
        buffer.putInt(chunk.getChunkZ());
        buffer.putBoolean(fullChunk);


        int mask = 0;
        Buffer blocks = Buffer.create();
        for (int i = 0; i < 16; i++) {
            // TODO if fullchunk is false then only send changed sections
            mask |= 1 << i;
            Short[] section = getSection(chunk, i);
            Utils.writeBlocks(blocks, section, 14);
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
        Utils.writeVarInt(buffer, mask);

        // Heightmap
        int[] motionBlocking = new int[16 * 16];
        int[] worldSurface = new int[16 * 16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                motionBlocking[x + z * 16] = 4;
                worldSurface[x + z * 16] = 5;
            }
        }
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
        buffer.putBytes(data);

        Utils.writeVarInt(buffer, blocks.length());
        buffer.putBuffer(blocks);
        Utils.writeVarInt(buffer, 0);
    }

    private Short[] getSection(Chunk chunk, int section) {
        Short[] blocks = new Short[16 * 16 * 16];
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int index = (((y * 16) + x) * 16) + z;
                    blocks[index] = chunk.getBlockId(x, y + 16 * section, z);
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
