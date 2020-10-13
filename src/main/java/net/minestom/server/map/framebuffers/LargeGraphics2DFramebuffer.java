package net.minestom.server.map.framebuffers;

import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.LargeFramebuffer;
import net.minestom.server.map.MapColors;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * {@link LargeFramebuffer} that embeds a {@link BufferedImage},
 * allowing for rendering directly via {@link Graphics2D} or its pixel array.
 */
public class LargeGraphics2DFramebuffer implements LargeFramebuffer {

    private final BufferedImage backingImage;
    private final Graphics2D renderer;
    private final int[] pixels;
    private final int width;
    private final int height;

    public LargeGraphics2DFramebuffer(int width, int height) {
        this.width = width;
        this.height = height;
        backingImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        renderer = backingImage.createGraphics();
        pixels = ((DataBufferInt) backingImage.getRaster().getDataBuffer()).getData();
    }

    public Graphics2D getRenderer() {
        return renderer;
    }

    public BufferedImage getBackingImage() {
        return backingImage;
    }

    public int get(int x, int z) {
        return pixels[x + z * width]; // stride is always the width of the image
    }

    public LargeGraphics2DFramebuffer set(int x, int z, int rgb) {
        pixels[x + z * width] = rgb;
        return this;
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

    @Override
    public byte getMapColor(int x, int y) {
        return MapColors.closestColor(get(x, y)).getIndex();
    }
}
