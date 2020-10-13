package net.minestom.server.map.framebuffers;

import net.minestom.server.map.Framebuffer;

/**
 * {@link Framebuffer} with direct access to the colors array
 */
public class DirectFramebuffer implements Framebuffer {

    private final byte[] colors = new byte[WIDTH * HEIGHT];

    /**
     * Mutable colors array
     *
     * @return
     */
    public byte[] getColors() {
        return colors;
    }

    public byte get(int x, int z) {
        return colors[Framebuffer.index(x, z)];
    }

    public DirectFramebuffer set(int x, int z, byte color) {
        colors[Framebuffer.index(x, z)] = color;
        return this;
    }

    @Override
    public byte[] toMapColors() {
        return colors;
    }
}
