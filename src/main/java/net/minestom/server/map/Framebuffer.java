package net.minestom.server.map;

/**
 * Framebuffer to render to a map
 */
public interface Framebuffer {

    int WIDTH = 128;
    int HEIGHT = 128;

    byte[] toMapColors();

    static int index(int x, int z) {
        return index(x, z, WIDTH);
    }

    static int index(int x, int z, int stride) {
        return z*stride + x;
    }

}
