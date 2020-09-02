package net.minestom.server.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.minestom.server.utils.PrimitiveConversion;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializableDataImpl extends DataImpl implements SerializableData {

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
        SerializableDataImpl data = new SerializableDataImpl();
        data.data.putAll(this.data);
        data.dataType.putAll(this.dataType);
        return data;
    }

    @Override
    public byte[] getSerializedData(Object2ShortMap<String> typeToIndexMap, boolean indexed) {
        // Get the current max index, it supposes that the index keep being incremented by 1
        short lastIndex = (short) typeToIndexMap.size();

        // Main buffer containing the data
        BinaryWriter binaryWriter = new BinaryWriter();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            final Class type = dataType.get(key);
            final short typeIndex;
            {
                // Find the type name
                final String encodedType = PrimitiveConversion.getObjectClassString(type.getName()); // Data type (fix for primitives)

                // Find the type index
                if (typeToIndexMap.containsKey(encodedType)) {
                    // Get index
                    typeIndex = typeToIndexMap.getShort(encodedType);
                } else {
                    // Create new index
                    typeToIndexMap.put(encodedType, ++lastIndex);
                    // Set index
                    typeIndex = lastIndex;
                }
            }


            // Write the data type index
            binaryWriter.writeShort(typeIndex);

            // Write the data key
            binaryWriter.writeSizedString(key);

            // Write the data (no length)
            final DataType dataType = DATA_MANAGER.getDataType(type);
            dataType.encode(binaryWriter, value);
        }

        binaryWriter.writeShort((short) 0); // End of data object

        // Header for type indexes
        if (indexed) {
            // The buffer containing all the index info (class name to class index)
            BinaryWriter indexWriter = new BinaryWriter();
            SerializableData.writeDataIndexHeader(indexWriter, typeToIndexMap);
            // Merge the index buffer & the main data buffer
            final ByteBuf finalBuffer = Unpooled.wrappedBuffer(indexWriter.getBuffer(), binaryWriter.getBuffer());
            // Change the main writer buffer, so it contains both the indexes and the data
            binaryWriter.setBuffer(finalBuffer);
        }

        return binaryWriter.toByteArray();
    }

    @Override
    public byte[] getIndexedSerializedData() {
        return getSerializedData(new Object2ShortOpenHashMap<>(), true);
    }

}
