package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class StringArrayData extends DataType<String[]> {

    @Override
    public void encode(BinaryWriter binaryWriter, String[] value) {
        binaryWriter.writeVarInt(value.length);
        for (String val : value) {
            binaryWriter.writeSizedString(val);
        }
    }

    @Override
    public String[] decode(BinaryReader binaryReader) {
        String[] array = new String[binaryReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = binaryReader.readSizedString();
        }
        return array;
    }
}
