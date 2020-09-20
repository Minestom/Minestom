package net.minestom.server.reader;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.DataManager;
import net.minestom.server.data.SerializableData;
import net.minestom.server.data.SerializableDataImpl;
import net.minestom.server.utils.binary.BinaryReader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class used to convert an array of bytes to a {@link SerializableData}
 * <p>
 * WARNING: the {@link DataManager} needs to have all the required types as the {@link SerializableData} has
 */
public class DataReader {

    private static final DataManager DATA_MANAGER = MinecraftServer.getDataManager();

    private static ConcurrentHashMap<String, Class> nameToClassMap = new ConcurrentHashMap<>();

    /**
     * Convert a buffer into a {@link SerializableData}, this will not read the data index header.
     * Use {@link #readIndexedData(BinaryReader)} to read the whole data object (if your data contains the indexes)
     * <p>
     * WARNING: the {@link DataManager} needs to have all the required types as the {@link SerializableData} has
     *
     * @param typeToIndexMap the map which index all the type contained in the data (className-&gt;classIndex)
     * @param reader         the reader
     * @return a {@link SerializableData} based on the data input
     */
    public static SerializableData readData(Object2ShortMap<String> typeToIndexMap, BinaryReader reader) {
        final Short2ObjectMap<String> indexToTypeMap = new Short2ObjectOpenHashMap<>(typeToIndexMap.size());
        {
            // Fill the indexToType map
            for (Object2ShortMap.Entry<String> entry : typeToIndexMap.object2ShortEntrySet()) {
                final String type = entry.getKey();
                final short index = entry.getShortValue();
                indexToTypeMap.put(index, type);
            }
        }

        SerializableData data = new SerializableDataImpl();
        while (true) {
            // Get the class index
            final short typeIndex = reader.readShort();

            if (typeIndex == 0) {
                // End of data
                break;
            }

            final Class type;
            {
                final String className = indexToTypeMap.get(typeIndex);
                type = nameToClassMap.computeIfAbsent(className, s -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
            }

            // Get the key
            final String name = reader.readSizedString();

            // Get the data
            final Object value = DATA_MANAGER.getDataType(type).decode(reader);

            // Set the data
            data.set(name, value, type);
        }

        return data;
    }

    /**
     * Read the indexes of the data + the data
     *
     * @param reader the reader
     * @return the deserialized {@link SerializableData}
     */
    public static SerializableData readIndexedData(BinaryReader reader) {
        final Object2ShortMap<String> typeToIndexMap = readDataIndexes(reader);
        return readData(typeToIndexMap, reader);
    }

    /**
     * Get a map containing the indexes of your data (type name -&gt; type index)
     *
     * @param binaryReader the reader
     * @return a map containing the indexes of your data
     */
    public static Object2ShortMap<String> readDataIndexes(BinaryReader binaryReader) {
        Object2ShortMap<String> typeToIndexMap = new Object2ShortOpenHashMap<>();
        {
            final int dataIndexSize = binaryReader.readVarInt();
            for (int i = 0; i < dataIndexSize; i++) {
                final String className = binaryReader.readSizedString();
                final short classIndex = binaryReader.readShort();
                typeToIndexMap.put(className, classIndex);
            }
        }
        return typeToIndexMap;
    }

}
