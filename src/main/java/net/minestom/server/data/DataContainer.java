package net.minestom.server.data;

import net.minestom.server.io.DataReader;
import net.minestom.server.io.IOManager;
import net.minestom.server.utils.CompressionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public interface DataContainer {

    Data getData();

    void setData(Data data);

    default void saveData(File file, Runnable callback) {
        IOManager.submit(() -> {
            Data data = getData();
            if (data == null)
                throw new NullPointerException("You cannot save null data!");
            if (!(data instanceof SerializableData))
                throw new IllegalArgumentException("Only SerializableData can be serialized");
            SerializableData serializableData = (SerializableData) data;

            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] serializedData = serializableData.getSerializedData();
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

            SerializableData data = DataReader.readCompressedData(array);

            setData(data);
            if (callback != null)
                callback.accept(data);
        });

    }

    default void loadData(File file) {
        loadData(file, null);
    }

}