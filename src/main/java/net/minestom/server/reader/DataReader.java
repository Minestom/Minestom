package net.minestom.server.reader;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.DataManager;
import net.minestom.server.data.SerializableData;
import net.minestom.server.network.packet.PacketReader;

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
     * @param buffer the data
     * @return a {@link SerializableData} based on the data input
     */
    public static SerializableData readData(ByteBuf buffer) {
        SerializableData data = new SerializableData();
        try {
            while (true) {
                short typeLength = buffer.readShort();

                if (typeLength == 0xff) {
                    // End of data
                    break;
                }

                byte[] typeCache = new byte[typeLength];
                for (int i = 0; i < typeLength; i++) {
                    typeCache[i] = buffer.readByte();
                }

                String className = new String(typeCache);
                Class type = Class.forName(className);

                short nameLength = buffer.readShort();
                byte[] nameCache = new byte[nameLength];
                for (int i = 0; i < nameLength; i++) {
                    nameCache[i] = buffer.readByte();
                }

                ByteBuf valueCache = buffer.readBytes(buffer.readInt());

                String name = new String(nameCache);
                PacketReader packetReader = new PacketReader(valueCache);
                Object value = DATA_MANAGER.getDataType(type).decode(packetReader);

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
     * @see #readData(ByteBuf)
     */
    public static SerializableData readData(byte[] data) {
        return readData(Unpooled.wrappedBuffer(data));
    }

}
