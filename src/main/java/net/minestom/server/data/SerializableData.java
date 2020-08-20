package net.minestom.server.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.PrimitiveConversion;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.concurrent.ConcurrentHashMap;

public class SerializableData extends Data {

    private static final DataManager DATA_MANAGER = MinecraftServer.getDataManager();

    private ConcurrentHashMap<String, Class> dataType = new ConcurrentHashMap<>();

    /**
     * Set a value to a specific key
     * <p>
     * WARNING: the type needs to be registered in {@link DataManager}
     *
     * @param key   the key
     * @param value the value object
     * @param type  the value type
     * @param <T>   the value generic
     * @throws UnsupportedOperationException if {@code type} is not registered in {@link DataManager}
     */
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
        data.data.putAll(this.data);
        data.dataType.putAll(this.dataType);
        return data;
    }

    /**
     * Serialize the data into an array of bytes
     * <p>
     * Use {@link net.minestom.server.reader.DataReader#readData(byte[])}
     * to convert it back
     *
     * @return the array representation of this data object
     */
    public byte[] getSerializedData() {
        BinaryWriter binaryWriter = new BinaryWriter();

        data.forEach((key, value) -> {
            final Class type = dataType.get(key);
            final DataType dataType = DATA_MANAGER.getDataType(type);

            // Write the data type
            final String encodedType = PrimitiveConversion.getObjectClassString(type.getName()); // Data type (fix for primitives)
            binaryWriter.writeSizedString(encodedType);

            // Write the data key
            binaryWriter.writeSizedString(key);

            // Write the data (no length)
            dataType.encode(binaryWriter, value);
        });

        binaryWriter.writeVarInt(0); // End of data object

        return binaryWriter.toByteArray();
    }

}
