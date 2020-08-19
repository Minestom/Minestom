package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.data.SerializableData;
import net.minestom.server.reader.DataReader;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

// Pretty weird name huh?
public class SerializableDataData extends DataType<SerializableData> {

    @Override
    public void encode(BinaryWriter binaryWriter, SerializableData value) {
        binaryWriter.writeBytes(value.getSerializedData());
    }

    @Override
    public SerializableData decode(BinaryReader binaryReader) {
        return DataReader.readData(binaryReader);
    }
}
