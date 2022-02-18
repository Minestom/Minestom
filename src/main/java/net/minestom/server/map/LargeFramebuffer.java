package net.minestom.server.map;

import net.minestom.server.network.packet.server.play.MapDataPacket;

import java.util.List;

/**
 * Framebuffer that is meant to be split in sub-framebuffers.
 * Contrary to {@link Framebuffer}, LargeFramebuffer supports sizes over 128x128 pixels.
 */
public interface LargeFramebuffer {

    int width();

    int height();

    /**
     * Returns a new {@link Framebuffer} that represent a 128x128 sub-view of this framebuffer.
     * Implementations are free (but not guaranteed) to throw exceptions if left &amp; top produces out-of-bounds coordinates.
     *
     * @param left
     * @param top
     * @return the sub-view {@link Framebuffer}
     */
    Framebuffer createSubView(int left, int top);

    byte getMapColor(int x, int y);

    /**
     * Prepares the packet to render a 128x128 sub view of this framebuffer
     */
    default MapDataPacket preparePacket(int mapId, int left, int top) {
        byte[] colors = new byte[Framebuffer.WIDTH * Framebuffer.WIDTH];
        final int width = Math.min(width(), left + Framebuffer.WIDTH) - left;
        final int height = Math.min(height(), top + Framebuffer.HEIGHT) - top;
        for (int y = top; y < height; y++) {
            for (int x = left; x < width; x++) {
                final byte color = getMapColor(x, y);
                colors[Framebuffer.index(x - left, y - top)] = color;
            }
        }
        return new MapDataPacket(mapId, (byte) 0, false,
                false, List.of(),
                new MapDataPacket.ColorContent((byte) width, (byte) height,
                        (byte) 0, (byte) 0,
                        colors));
    }
}
