package net.minestom.server.command.builder;

import net.minestom.server.command.builder.structure.Structure;

import java.util.HashMap;
import java.util.Map;

public class Arguments {

    private Map<String, Object> args = new HashMap<>();

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

    public String[] getStringArray(String id) {
        return (String[]) getObject(id);
    }

    public Object getObject(String id) {
        return args.getOrDefault(id, null);
    }

    protected void setArg(String id, Object value) {
        this.args.put(id, value);
    }

}
