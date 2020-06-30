package net.minestom.server.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.PrimitiveConversion;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializableData extends Data {

    private static final DataManager DATA_MANAGER = MinecraftServer.getDataManager();

    private ConcurrentHashMap<String, Class> dataType = new ConcurrentHashMap<>();

    @Override
    public <T> void set(String key, T value, Class<T> type) {
        if (DATA_MANAGER.getDataType(type) == null) {
            throw new UnsupportedOperationException("Type " + type.getName() + " hasn't been registered in DataManager#registerType");
        }

        super.set(key, value, type);
        this.dataType.put(key, type);
    }

    @Override
    public Data clone() {
        SerializableData data = new SerializableData();
        data.data = new ConcurrentHashMap<>(this.data);
        data.dataType = new ConcurrentHashMap<>(dataType);
        return data;
    }

    public byte[] getSerializedData() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(output);

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Class type = dataType.get(key);
            Object value = entry.getValue();
            DataType dataType = DATA_MANAGER.getDataType(type);

            byte[] encodedType = PrimitiveConversion.getObjectClassString(type.getName()).getBytes(); // Data type (fix for primitives)
            dos.writeShort(encodedType.length);
            dos.write(encodedType);

            byte[] encodedName = key.getBytes(); // Data name
            dos.writeShort(encodedName.length);
            dos.write(encodedName);

            PacketWriter packetWriter = new PacketWriter();
            dataType.encode(packetWriter, value); // Encode
            byte[] encodedValue = packetWriter.toByteArray(); // Retrieve bytes
            dos.writeInt(encodedValue.length);
            dos.write(encodedValue);
        }

        dos.writeShort(0xff); // End of data object

        return output.toByteArray();
    }

}
