package net.minestom.server.storage.systems;

import net.minestom.server.storage.StorageOptions;
import net.minestom.server.storage.StorageSystem;
import org.rocksdb.*;

import java.io.File;

/**
 * A storage system which is local using OS files system
 * It does make use of the RocksDB library
 */
public class FileStorageSystem implements StorageSystem {

    static {
        RocksDB.loadLibrary();
    }

    private RocksDB rocksDB;

    @Override
    public boolean exists(String folderPath) {
        return new File(folderPath).exists();
    }

    @Override
    public void open(String folderPath, StorageOptions storageOptions) {
        Options options = new Options().setCreateIfMissing(true);

        if (storageOptions.hasCompression()) {
            options.setCompressionType(CompressionType.ZSTD_COMPRESSION);
            options.setCompressionOptions(new CompressionOptions().setLevel(1));
        }

        try {
            this.rocksDB = RocksDB.open(options, folderPath);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] get(String key) {
        try {
            byte[] result = this.rocksDB.get(getKey(key));
            return result;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void set(String key, byte[] data) {
        try {
            this.rocksDB.put(getKey(key), data);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String key) {
        try {
            this.rocksDB.delete(getKey(key));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            this.rocksDB.closeE();
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    private byte[] getKey(String key) {
        return key.getBytes();
    }

}
