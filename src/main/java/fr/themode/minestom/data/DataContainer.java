package fr.themode.minestom.data;

import fr.themode.minestom.Main;
import fr.themode.minestom.io.IOManager;
import fr.themode.minestom.utils.CompressionUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Consumer;

public interface DataContainer {

    Data getData();

    void setData(Data data);

    default void saveData(File file, Runnable callback) {
        IOManager.submit(() -> {
            Data data = getData();
            if (data == null) {
                // TODO error trying to save null data
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] serializedData = data.getSerializedData();
                fos.write(CompressionUtils.getCompressedData(serializedData));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (callback != null)
                callback.run();
        });
    }

    default void saveData(File file) {
        saveData(file, null);
    }

    default void loadData(File file, Consumer<Data> callback) {
        IOManager.submit(() -> {

            if (!file.exists()) {
                setData(new Data());
                if (callback != null)
                    callback.accept(getData());
                System.out.println("FILE DATA NOT FOUND, NEW DATA OBJECT CREATED");
                return;
            }

            byte[] array;
            try {
                array = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                e.printStackTrace(); // Unknown error
                return;
            }

            DataInputStream stream = new DataInputStream(new ByteArrayInputStream(CompressionUtils.getDecompressedData(array)));

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
                    byte[] valueCache = new byte[valueLength];
                    for (int i = 0; i < valueLength; i++) {
                        valueCache[i] = stream.readByte();
                    }

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

            setData(data);
            if (callback != null)
                callback.accept(data);
        });

    }

    default void loadData(File file) {
        loadData(file, null);
    }

}