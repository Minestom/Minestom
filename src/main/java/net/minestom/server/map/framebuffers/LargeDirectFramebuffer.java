package net.minestom.server.map.framebuffers;

import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.LargeFramebuffer;
import net.minestom.server.map.MapColors;

/**
 * {@link LargeFramebuffer} with direct access to the colors array.
 * <p>
 * This implementation does not throw errors when accessing out-of-bounds coordinates through sub-views, and will instead
 * use {@link MapColors#NONE}. This is only the case for sub-views, access through {@link #setMapColor(int, int, byte)}
 * and {@link #getMapColor(int, int)} will throw an exception if out-of-bounds coordinates are inputted.
 */
public class LargeDirectFramebuffer implements LargeFramebuffer {

    private final int width;
    private final int height;
    private final byte[] colors;

    /**
     * Creates a new {@link LargeDirectFramebuffer} with the desired size
     *
     * @param width
     * @param height
     */
    public LargeDirectFramebuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.colors = new byte[width * height];
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public Framebuffer createSubView(int left, int top) {
        return new LargeFramebufferDefaultView(this, left, top);
    }

    public LargeDirectFramebuffer setMapColor(int x, int y, byte color) {
        if (!bounds(x, y)) throw new IndexOutOfBoundsException("Invalid x;y coordinate: " + x + ";" + y);
        colors[y * width + x] = color;
        return this;
    }

    @Override
    public byte getMapColor(int x, int y) {
        if (!bounds(x, y)) throw new IndexOutOfBoundsException("Invalid x;y coordinate: " + x + ";" + y);
        return colors[y * width + x];
    }

    private boolean bounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public byte[] getColors() {
        return colors;
    }
}
