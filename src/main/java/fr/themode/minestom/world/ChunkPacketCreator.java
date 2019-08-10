package fr.themode.minestom.world;

import fr.adamaq01.ozao.net.Buffer;
import fr.adamaq01.ozao.net.packet.Packet;
import fr.themode.minestom.utils.Utils;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.LongArrayTag;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChunkPacketCreator {

    public static Packet create(int chunkX, int chunkZ, CustomChunk customChunk, int start, int end) {
        Packet packet = Packet.create();
        packet.put("id", 0x21);
        Buffer payload = packet.getPayload();

        payload.putInt(chunkX);
        payload.putInt(chunkZ);
        payload.putBoolean(true); // Send biome data (loading chunk, not modifying it)
        int mask = 0;
        Buffer blocks = Buffer.create();
        for (int i = 0; i < 16; i++) {
            mask |= 1 << i;
            CustomBlock[] section = getSection(customChunk, i);
            Utils.writeBlocks(blocks, section, 14);
        }
        // Biome data
        int[] biomeData = new int[256];
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                biomeData[z * 16 | x] = customChunk.getBiome().getId();
            }
        }
        for (int i = 0; i < biomeData.length; i++) {
            blocks.putInt(biomeData[i]);
        }
        Utils.writeVarInt(payload, mask);

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
        payload.putBytes(data);

        Utils.writeVarInt(payload, blocks.length());
        payload.putBuffer(blocks);
        Utils.writeVarInt(payload, 0);

        return packet;
    }

    public static CustomBlock[] getSection(CustomChunk customChunk, int section) {
        CustomBlock[] blocks = new CustomBlock[16 * 16 * 16];
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int index = (((y * 16) + x) * 16) + z;
                    blocks[index] = customChunk.getBlock(x, y + 16 * section, z);
                }
            }
        }
        return blocks;
    }
}
