package net.minestom.server.storage;

@Deprecated
public class StorageOptions {

    private boolean compression;

    /**
     * Gets if compression should be enabled.
     *
     * @return true if compression should be enabled, false otherwise
     */
    public boolean hasCompression() {
        return compression;
    }

    /**
     * Defines if the storage solution should use compression.
     *
     * @param compression true to enable compression, false otherwise
     * @return the reference to the current options
     */
    public StorageOptions setCompression(boolean compression) {
        this.compression = compression;
        return this;
    }
}
