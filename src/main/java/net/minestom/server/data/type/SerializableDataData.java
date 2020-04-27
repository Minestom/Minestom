package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.data.SerializableData;
import net.minestom.server.io.DataReader;

import java.io.IOException;

// Pretty weird name huh?
public class SerializableDataData extends DataType<SerializableData> {

    @Override
    public byte[] encode(SerializableData value) {
        try {
            return value.getSerializedData();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("error while writing the data");
        }
    }

    @Override
    public SerializableData decode(byte[] value) {
        return DataReader.readData(value, false);
    }
}
