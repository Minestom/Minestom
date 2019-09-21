package fr.themode.minestom.io;

import fr.themode.minestom.Main;
import fr.themode.minestom.data.Data;
import fr.themode.minestom.utils.CompressionUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class DataReader {

    public static Data readData(byte[] b, boolean shouldDecompress) {
        b = shouldDecompress ? CompressionUtils.getDecompressedData(b) : b;

        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(b));

        Data data = new Data();
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
                Object value = Main.getDataManager().getDataType(type).decode(valueCache);

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
