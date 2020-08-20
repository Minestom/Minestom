package net.minestom.server.extensions;

import com.google.gson.JsonObject;
import org.slf4j.Logger;

public abstract class Extension {
    private JsonObject description;
    private Logger logger;

    protected Extension() {

    }

    public void preInitialize() {

    }

    public abstract void initialize();

    public void postInitialize() {

    }

    public void preTerminate() {

    }

    public abstract void terminate();

    public void postTerminate() {

    }

    public JsonObject getDescription() {
        return description;
    }

    protected Logger getLogger() {
        return logger;
    }
}
