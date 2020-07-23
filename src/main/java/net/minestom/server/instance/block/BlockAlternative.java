package net.minestom.server.instance.block;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BlockAlternative {

    private final short id;
    private final String[] properties;

    public BlockAlternative(short id, String... properties) {
        this.id = id;
        this.properties = properties;
    }

    public short getId() {
        return id;
    }

    public String[] getProperties() {
        return properties;
    }

    public Map<String, String> createPropertiesMap() {
        Map<String, String> map = new HashMap<>();
        for (String p : properties) {
            String[] parts = p.split("=");
            map.put(parts[0], parts[1]);
        }
        return map;
    }

    @Override
    public String toString() {
        return "BlockAlternative{" +
                "id=" + id +
                ", properties=" + Arrays.toString(properties) +
                '}';
    }
}
