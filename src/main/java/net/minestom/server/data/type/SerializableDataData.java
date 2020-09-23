package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.data.SerializableData;
import net.minestom.server.data.SerializableDataImpl;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

// Pretty weird name huh?
public class SerializableDataData extends DataType<SerializableData> {

    @Override
    public void encode(BinaryWriter writer, SerializableData value) {
        writer.writeBytes(value.getIndexedSerializedData());
    }

    @Override
    public SerializableData decode(BinaryReader reader) {
        SerializableData serializableData = new SerializableDataImpl();
        serializableData.readIndexedSerializedData(reader);
        return serializableData;
    }
}
