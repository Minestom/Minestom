package net.minestom.server.storage;

public class StorageOptions {

    private boolean compression;

    /**
     * Get if compression should be enabled
     *
     * @return true if compression should be enabled, false otherwise
     */
    public boolean hasCompression() {
        return compression;
    }

    /**
     * Define if the storage solution should use compression
     *
     * @param compression true to enable compression, false otherwise
     * @return the reference to the current options
     */
    public StorageOptions setCompression(boolean compression) {
        this.compression = compression;
        return this;
    }
}
