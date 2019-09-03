package fr.themode.minestom.utils;

import com.github.luben.zstd.Zstd;

public class CompressionUtils {

    private static final int COMPRESSION_LEVEL = 1;

    public static byte[] getCompressedData(byte[] decompressed) {
        byte[] decompressedLength = SerializerUtils.intToBytes(decompressed.length);
        byte[] compressed = Zstd.compress(decompressed, COMPRESSION_LEVEL);

        byte[] result = new byte[decompressedLength.length + compressed.length];
        System.arraycopy(decompressedLength, 0, result, 0, decompressedLength.length);
        System.arraycopy(compressed, 0, result, decompressedLength.length, compressed.length);

        return result;
    }

    public static byte[] getDecompressedData(byte[] compressed) {
        int decompressedLength = SerializerUtils.bytesToInt(compressed);

        byte[] compressedChunkData = new byte[compressed.length - Integer.BYTES];
        System.arraycopy(compressed, Integer.BYTES, compressedChunkData, 0, compressedChunkData.length); // Remove the decompressed length from the array

        byte[] decompressed = new byte[decompressedLength];
        long result = Zstd.decompress(decompressed, compressedChunkData); // Decompressed in an array with the max size

        compressed = new byte[(int) result];
        System.arraycopy(decompressed, 0, compressed, 0, (int) result); // Resize the data array properly

        return compressed;
    }


}
