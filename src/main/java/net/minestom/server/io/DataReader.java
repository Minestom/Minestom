package net.minestom.server.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.DataManager;
import net.minestom.server.data.SerializableData;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.utils.CompressionUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class DataReader {

    private static final DataManager DATA_MANAGER = MinecraftServer.getDataManager();

    public static SerializableData readData(byte[] b, boolean shouldDecompress) {
        b = shouldDecompress ? CompressionUtils.getDecompressedData(b) : b;

        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(b));

        SerializableData data = new SerializableData();
        try {
            while (true) {
                short typeLength = stream.readShort();

                if (typeLength == 0xff) {
                    // End of data
                    break;
                }

                byte[] typeCache = new byte[typeLength];
                for (int i = 0; i < typeLength; i++) {
                    typeCache[i] = stream.readByte();
                }

                short nameLength = stream.readShort();
                byte[] nameCache = new byte[nameLength];
                for (int i = 0; i < nameLength; i++) {
                    nameCache[i] = stream.readByte();
                }

                int valueLength = stream.readInt();
                byte[] valueCache = stream.readNBytes(valueLength);

                Class type = Class.forName(new String(typeCache));

                String name = new String(nameCache);
                ByteBuf buffer = Unpooled.wrappedBuffer(valueCache);
                PacketReader packetReader = new PacketReader(buffer);
                Object value = DATA_MANAGER.getDataType(type).decode(packetReader, valueCache);

                data.set(name, value, type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return data;
    }

}
