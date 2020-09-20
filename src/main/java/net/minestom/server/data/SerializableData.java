package net.minestom.server.data;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.reader.DataReader;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

/**
 * Represent a {@link Data} object which can be serialized and read back by the {@link DataReader}
 */
public interface SerializableData extends Data {

    DataManager DATA_MANAGER = MinecraftServer.getDataManager();

    /**
     * Serialize the data into an array of bytes
     * <p>
     * Use {@link DataReader#readIndexedData(BinaryReader)} if {@code indexed} is true,
     * {@link DataReader#readData(Object2ShortMap, BinaryReader)} otherwise with the index map
     * to convert it back to a {@link SerializableData}
     *
     * @param typeToIndexMap the type to index map, will create entries if new types are discovered.
     *                       The map is not thread-safe
     * @param indexed        true to add the types index in the header
     * @return the array representation of this data object
     */
    byte[] getSerializedData(Object2ShortMap<String> typeToIndexMap, boolean indexed);

    /**
     * Serialize the data into an array of bytes
     * <p>
     * Use {@link net.minestom.server.reader.DataReader#readIndexedData(BinaryReader)}
     * to convert it back to a {@link SerializableData}
     * <p>
     * This will create a type index map which will be present in the header
     *
     * @return the array representation of this data object
     */
    byte[] getIndexedSerializedData();

    /**
     * Get the index info (class name -&gt; class index)
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

}
