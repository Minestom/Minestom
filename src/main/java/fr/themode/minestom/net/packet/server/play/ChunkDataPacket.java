package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class ChunkDataPacket implements ServerPacket {

    public int columnX;
    public int columnZ;
    public boolean fullChunk;
    public int mask;
    public ChunkSection[] chunkSections;
    public int tileEntitesSize;
    // TODO nbt tile entities

    @Override
    public void write(Buffer buffer) {
        buffer.putInt(columnX);
        buffer.putInt(columnZ);
        buffer.putBoolean(fullChunk);
        Utils.writeVarInt(buffer, mask);
        System.out.println("test: " + getDataSize());
        Utils.writeVarInt(buffer, getDataSize());
        writeData(buffer);
        Utils.writeVarInt(buffer, tileEntitesSize);
        // TODO nbt tile entities
    }

    private int getDataSize() {
        int result = Integer.BYTES * 256; // Size for 256 biomes value
        for (int i = 0; i < chunkSections.length; i++) {
            result += chunkSections[i].getSize();
        }
        return result;
    }

    private void writeData(Buffer buffer) {
        for (ChunkSection chunkSection : chunkSections) {
            chunkSection.write(buffer);
        }
        // Biomes data
        for (int i = 0; i < 256; i++) {
            buffer.putInt(127); // Void biome
        }
    }

    @Override
    public int getId() {
        return 0x21;
    }

    public static class ChunkSection {

        public byte bitsPerBlock;
        public int paletteLength; // Optional
        public int[] palette; // Optional
        public long[] data;
        public byte[] blockLight;
        public byte[] skyLight;

        public void write(Buffer buffer) {
            buffer.putByte(bitsPerBlock);

            if (bitsPerBlock < 9) {
                Utils.writeVarInt(buffer, paletteLength);
                for (int p : palette) {
                    Utils.writeVarInt(buffer, p);
                }
            }

            Utils.writeVarInt(buffer, data.length);
            for (long d : data) {
                buffer.putLong(d);
            }
            //buffer.putBytes(blockLight);
            //buffer.putBytes(skyLight);
        }

        public int getSize() {
            int size = 0;
            size++; //bitsPerBlock
            if (bitsPerBlock < 9) {
                size += Utils.lengthVarInt(paletteLength);
                for (int p : palette) {
                    size += Utils.lengthVarInt(p);
                }
            }

            size += Utils.lengthVarInt(data.length);
            size += Long.BYTES * data.length;
            //size += blockLight.length;
            //size += skyLight.length;
            return size;
        }

    }
}
