package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.data.SerializableData;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.reader.DataReader;

import java.io.IOException;

// Pretty weird name huh?
public class SerializableDataData extends DataType<SerializableData> {

    @Override
    public void encode(PacketWriter packetWriter, SerializableData value) {
        try {
            packetWriter.writeBytes(value.getSerializedData());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("error while writing the data");
        }
    }

    @Override
    public SerializableData decode(PacketReader packetReader) {
        return DataReader.readData(packetReader.getBuffer());
    }
}
