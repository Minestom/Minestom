package net.minestom.server.map.framebuffers;

import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.MapColors;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * {@link Framebuffer} that embeds a BufferedImage, allowing for rendering directly via Graphics2D or its pixel array.
 */
public class Graphics2DFramebuffer implements Framebuffer {

    private final byte[] colors = new byte[WIDTH * HEIGHT];
    private final BufferedImage backingImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private final Graphics2D renderer;
    private final int[] pixels;

    public Graphics2DFramebuffer() {
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
        return pixels[x + z * WIDTH]; // stride is always the width of the image
    }

    public Graphics2DFramebuffer set(int x, int z, int rgb) {
        pixels[x + z * WIDTH] = rgb;
        return this;
    }

    @Override
    public byte[] toMapColors() {
        // TODO: update subparts only
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                colors[Framebuffer.index(x, z)] = MapColors.closestColor(get(x, z)).getIndex();
            }
        }
        return colors;
    }
}
