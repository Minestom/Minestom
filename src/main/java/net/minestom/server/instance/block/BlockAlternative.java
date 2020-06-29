package net.minestom.server.instance.block;

import java.util.Arrays;

public class BlockAlternative {

    private short id;
    private String[] properties;

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

    @Override
    public String toString() {
        return "BlockAlternative{" +
                "id=" + id +
                ", properties=" + Arrays.toString(properties) +
                '}';
    }
}
