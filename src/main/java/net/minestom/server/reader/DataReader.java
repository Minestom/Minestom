package net.minestom.server.reader;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.DataManager;
import net.minestom.server.data.SerializableData;
import net.minestom.server.utils.binary.BinaryReader;

/**
 * Class used to convert an array of bytes to a {@link SerializableData}
 * <p>
 * WARNING: the {@link DataManager} needs to have all the required types as the {@link SerializableData} has
 */
public class DataReader {

    private static final DataManager DATA_MANAGER = MinecraftServer.getDataManager();

    /**
     * Convert a buffer into a {@link SerializableData}
     * <p>
     * WARNING: the {@link DataManager} needs to have all the required types as the {@link SerializableData} has
     *
     * @param reader the reader
     * @return a {@link SerializableData} based on the data input
     */
    public static SerializableData readData(BinaryReader reader) {
        SerializableData data = new SerializableData();
        try {
            while (true) {
                final int typeLength = reader.readVarInt();

                if (typeLength == 0) {
                    // End of data
                    break;
                }

                // Get the class type
                final Class type;
                {
                    final byte[] typeCache = reader.readBytes(typeLength);

                    final String className = new String(typeCache);

                    type = Class.forName(className);
                }

                // Get the key
                final String name = reader.readSizedString();

                // Get the data
                final Object value = DATA_MANAGER.getDataType(type).decode(reader);

                // Set the data
                data.set(name, value, type);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Convert a bytes array to a {@link SerializableData}
     *
     * @param data the data
     * @return a {@link SerializableData} based on the data input
     * @see #readData(BinaryReader)
     */
    public static SerializableData readData(byte[] data) {
        return readData(new BinaryReader(data));
    }

}
