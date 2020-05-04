package net.minestom.server.reader;

import io.netty.buffer.ByteBuf;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.DataManager;
import net.minestom.server.data.SerializableData;
import net.minestom.server.network.packet.PacketReader;

public class DataReader {

    private static final DataManager DATA_MANAGER = MinecraftServer.getDataManager();

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

}
