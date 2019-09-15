package fr.themode.minestom.data;

import fr.themode.minestom.Main;
import fr.themode.minestom.utils.PrimitiveConversion;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Data {

    private static final DataManager DATA_MANAGER = Main.getDataManager();

    // TODO replace maps for something more memory-friendly
    private ConcurrentHashMap<String, Object> data = new ConcurrentHashMap();
    private ConcurrentHashMap<String, Class> dataType = new ConcurrentHashMap<>();

    public <T> void set(String key, T value, Class<T> type) {
        if (DATA_MANAGER.getDataType(type) == null) {
            throw new UnsupportedOperationException("Type " + type.getName() + " hasn't been registered in DataManager#registerType");
        }
        this.data.put(key, value);
        this.dataType.put(key, type);
    }

    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    public byte[] getSerializedData() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(output);

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Class type = dataType.get(key);
            Object value = entry.getValue();
            DataType dataType = Main.getDataManager().getDataType(type);

            byte[] encodedType = PrimitiveConversion.getObjectClassString(type.getName()).getBytes(); // Data type (fix for primitives)
            dos.writeShort(encodedType.length);
            dos.write(encodedType);

            byte[] encodedName = key.getBytes(); // Data name
            dos.writeShort(encodedName.length);
            dos.write(encodedName);

            byte[] encodedValue = dataType.encode(value); // Data
            dos.writeInt(encodedValue.length);
            dos.write(encodedValue);
        }

        dos.writeShort(0xff); // End of data object

        return output.toByteArray();
    }

}
