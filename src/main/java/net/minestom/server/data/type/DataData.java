package net.minestom.server.data.type;

import net.minestom.server.data.Data;
import net.minestom.server.data.DataType;
import net.minestom.server.io.DataReader;

import java.io.IOException;

// Pretty weird name huh?
public class DataData extends DataType<Data> {
    @Override
    public byte[] encode(Data value) {
        try {
            return value.getSerializedData();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("error while writing the data");
        }
    }

    @Override
    public Data decode(byte[] value) {
        return DataReader.readData(value, false);
    }
}
