package net.minestom.server.instance;

public class Heightmap {
    private final int[] values;

    public Heightmap() {
        values = new int[16 * 16];
    }

    public int get(int x, int z) {
        return values[index(x, z)];
    }

    public void set(int x, int z, int height) {
        values[index(x, z)] = height;
    }

    private int index(int x, int z) {
        return x + z * 16;
    }

    public int[] getValues() {
        return values;
    }
}
