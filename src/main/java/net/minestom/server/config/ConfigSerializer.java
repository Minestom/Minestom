package net.minestom.server.config;

public final class ConfigSerializer {
    private ConfigSerializer() {
        //no instance
    }

    /**
     * Used to create a record that holds fields that should be serialized
     */
    public static Record configToRecord(Config config) {
        // The config is currently backed by a record, so we can just return that
        return (Record) config;
    }
}
