package net.minestom.server.data;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link Data} object which can be serialized and read back.
 * <p>
 * See {@link SerializableDataImpl} for the default implementation.
 */
@Deprecated
public abstract class SerializableData extends Data {

    /**
     * Serializes the data into an array of bytes.
     * <p>
     * Use {@link #readIndexedSerializedData(BinaryReader)} if {@code indexed} is true,
     * {@link #readSerializedData(BinaryReader, Object2ShortMap)} otherwise with the index map
     * to convert it back to a {@link SerializableData}.
     *
     * @param typeToIndexMap the type to index map, will create entries if new types are discovered.
     *                       The map is not thread-safe
     * @param indexed        true to add the types index in the header
     * @return the array representation of this data object
     */
    @NotNull
    public abstract byte[] getSerializedData(@NotNull Object2ShortMap<String> typeToIndexMap, boolean indexed);

    /**
     * Reads the data of a {@link SerializableData} when you already have the index map.
     * <p>
     * WARNING: the data to read should not have any index to read and your index map should be COMPLETE.
     * Use {@link #readIndexedSerializedData(BinaryReader)} if you need to read the header.
     *
     * @param reader         the binary reader
     * @param typeToIndexMap the index map
     */
    public abstract void readSerializedData(@NotNull BinaryReader reader, @NotNull Object2ShortMap<String> typeToIndexMap);

    /**
     * Serializes the data into an array of bytes.
     * <p>
     * Use {@link #readIndexedSerializedData(BinaryReader)}
     * to convert it back to a {@link SerializableData}.
     * <p>
     * This will create a type index map which will be present in the header.
     *
     * @return the array representation of this data object
     */
    @NotNull
    public byte[] getIndexedSerializedData() {
        return getSerializedData(new Object2ShortOpenHashMap<>(), true);
    }

    /**
     * Reads the index map and the data of a serialized {@link SerializableData}.
     * <p>
     * Got from {@link #getIndexedSerializedData()}.
     *
     * @param reader the binary reader
     */
    public void readIndexedSerializedData(@NotNull BinaryReader reader) {
        final Object2ShortMap<String> typeToIndexMap = SerializableData.readDataIndexes(reader);
        readSerializedData(reader, typeToIndexMap);
    }

    @NotNull
    @Override
    public SerializableData clone() {
        return (SerializableData) super.clone();
    }

    /**
     * Writes the index info (class name -&gt; class index), used to write the header for indexed serialized data.
     * <p>
     * Sized by a var-int.
     *
     * @param typeToIndexMap the filled data index map
     */
    public static void writeDataIndexHeader(@NotNull BinaryWriter indexWriter, @NotNull Object2ShortMap<String> typeToIndexMap) {
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
     * Reads a data index map (type name -&gt; type index).
     * <p>
     * Can then be used with {@link SerializableData#readSerializedData(BinaryReader, Object2ShortMap)}.
     *
     * @param binaryReader the reader
     * @return a map containing the indexes of your data
     */
    @NotNull
    public static Object2ShortMap<String> readDataIndexes(@NotNull BinaryReader binaryReader) {
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
