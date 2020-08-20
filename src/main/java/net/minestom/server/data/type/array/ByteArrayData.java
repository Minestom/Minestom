package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class ByteArrayData extends DataType<byte[]> {
    @Override
    public void encode(BinaryWriter writer, byte[] value) {
        encodeByteArray(writer, value);
    }

    @Override
    public byte[] decode(BinaryReader reader) {
        return decodeByteArray(reader);
    }

    public static void encodeByteArray(BinaryWriter binaryWriter, byte[] value) {
        binaryWriter.writeVarInt(value.length);
        for (byte val : value) {
            binaryWriter.writeByte(val);
        }
    }

    public static byte[] decodeByteArray(BinaryReader binaryReader) {
        byte[] array = new byte[binaryReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = binaryReader.readByte();
        }
        return array;
    }
}
