package net.minestom.server.command.builder.structure;

import java.util.HashMap;
import java.util.Map;

public class Structure {

    private Map<String, Object> values = new HashMap<>();

    public Structure getStructure(String id) {
        return (Structure) getObject(id);
    }

    public boolean getBoolean(String id) {
        return (boolean) getObject(id);
    }

    public long getLong(String id) {
        return (long) getObject(id);
    }

    public int getInteger(String id) {
        return (int) getObject(id);
    }

    public double getDouble(String id) {
        return (double) getObject(id);
    }

    public float getFloat(String id) {
        return (float) getObject(id);
    }

    public String getString(String id) {
        return (String) getObject(id);
    }

    public String getWord(String id) {
        return getString(id);
    }

    public Object getObject(String id) {
        return values.getOrDefault(id, null);
    }

    public void setValue(String key, Object value) {
        this.values.put(key, value);
    }

}
