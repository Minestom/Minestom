package net.minestom.server.utils;

import net.minestom.server.config.Config;

public final class ConfigRecord {
    private ConfigRecord() {
        //no instance
    }

    /**
     * Used to create a record that holds fields that should be serialized
     */
    public static Object from(Config config) {
        // The config currently backed by a record, so we can just return that
        return config;
    }
}
