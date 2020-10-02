package net.minestom.server.data;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

/**
 * Represent a {@link Data} object which can be serialized and read back
 */
public interface SerializableData extends Data {

    DataManager DATA_MANAGER = MinecraftServer.getDataManager();

    /**
     * Serialize the data into an array of bytes
     * <p>
     * Use {@link #readIndexedSerializedData(BinaryReader)} if {@code indexed} is true,
     * {@link #readSerializedData(BinaryReader, Object2ShortMap)} otherwise with the index map
     * to convert it back to a {@link SerializableData}
     *
     * @param typeToIndexMap the type to index map, will create entries if new types are discovered.
     *                       The map is not thread-safe
     * @param indexed        true to add the types index in the header
     * @return the array representation of this data object
     */
    byte[] getSerializedData(Object2ShortMap<String> typeToIndexMap, boolean indexed);

    /**
     * Read the data of a {@link SerializableData} when you already have the index map
     * <p>
     * WARNING: the data to read should not have any index to read and your index map should be COMPLETE
     * Use {@link #readIndexedSerializedData(BinaryReader)} if you need to read the header
     *
     * @param reader         the binary reader
     * @param typeToIndexMap the index map
     */
    void readSerializedData(BinaryReader reader, Object2ShortMap<String> typeToIndexMap);

    /**
     * Serialize the data into an array of bytes
     * <p>
     * Use {@link #readIndexedSerializedData(BinaryReader)}
     * to convert it back to a {@link SerializableData}
     * <p>
     * This will create a type index map which will be present in the header
     *
     * @return the array representation of this data object
     */
    default byte[] getIndexedSerializedData() {
        return getSerializedData(new Object2ShortOpenHashMap<>(), true);
    }

    /**
     * Read the index map and the data of a serialized {@link SerializableData}
     * Got from {@link #getIndexedSerializedData()}
     *
     * @param reader the binary reader
     */
    default void readIndexedSerializedData(BinaryReader reader) {
        final Object2ShortMap<String> typeToIndexMap = SerializableData.readDataIndexes(reader);
        readSerializedData(reader, typeToIndexMap);
    }

    /**
     * Write the index info (class name -&gt; class index), used to write the header for indexed serialized data
     * <p>
     * Sized by a var-int
     *
     * @param typeToIndexMap the data index map
     */
    static void writeDataIndexHeader(BinaryWriter indexWriter, Object2ShortMap<String> typeToIndexMap) {
        // Write the size of the following index list (class name-> class index)
        indexWriter.writeVarInt(typeToIndexMap.size());

        for (Object2ShortMap.Entry<String> entry : typeToIndexMap.object2ShortEntrySet()) {
            final String className = entry.getKey();
            final short classIndex = entry.getShortValue();

            // Write className -> class index
            indexWriter.writeSizedString(className);
            indexWriter.writeShort(classIndex);

        }
    }

    /**
     * Read a data index map (type name -&gt; type index)
     * <p>
     * Can then be used with {@link SerializableData#readSerializedData(BinaryReader, Object2ShortMap)}
     *
     * @param binaryReader the reader
     * @return a map containing the indexes of your data
     */
    static Object2ShortMap<String> readDataIndexes(BinaryReader binaryReader) {
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
