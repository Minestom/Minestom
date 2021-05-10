package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.data.SerializableData;
import net.minestom.server.data.SerializableDataImpl;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

// Pretty weird name huh?
public class SerializableDataData extends DataType<SerializableData> {

    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull SerializableData value) {
        writer.writeBytes(value.getIndexedSerializedData());
    }

    @NotNull
    @Override
    public SerializableData decode(@NotNull BinaryReader reader) {
        SerializableData serializableData = new SerializableDataImpl();
        serializableData.readIndexedSerializedData(reader);
        return serializableData;
    }
}
