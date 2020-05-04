package net.minestom.server.storage.systems;

import net.minestom.server.storage.StorageSystem;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.function.Consumer;

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
    public void open(String folderName) {
        Options options = new Options().setCreateIfMissing(true);

        try {
            this.rocksDB = RocksDB.open(options, folderName);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void get(String key, Consumer<byte[]> callback) {
        try {
            byte[] result = this.rocksDB.get(getKey(key));
            callback.accept(result);
        } catch (RocksDBException e) {
            e.printStackTrace();
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
