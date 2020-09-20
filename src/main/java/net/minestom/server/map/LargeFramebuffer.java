package net.minestom.server.map;

import net.minestom.server.network.packet.server.play.MapDataPacket;

/**
 * Framebuffer that is meant to be split in sub-framebuffers. Contrary to Framebuffer, LargeFramebuffer supports sizes over 128x128 pixels.
 */
public interface LargeFramebuffer {

    int width();
    int height();

    /**
     * Returns a new Framebuffer that represent a 128x128 sub-view of this framebuffer.
     * Implementations are free (but not guaranteed) to throw exceptions if left &amp; top produces out-of-bounds coordinates.
     * @param left
     * @param top
     * @return
     */
    Framebuffer createSubView(int left, int top);

    byte getMapColor(int x, int y);

    /**
     * Prepares the packet to render a 128x128 sub view of this framebuffer
     * @param packet
     * @param left
     * @param top
     */
    default void preparePacket(MapDataPacket packet, int left, int top) {
        byte[] colors = new byte[Framebuffer.WIDTH*Framebuffer.WIDTH];
        int width = Math.min(width(), left+Framebuffer.WIDTH) - left;
        int height = Math.min(height(), top+Framebuffer.HEIGHT) - top;
        for (int y = top; y < height; y++) {
            for (int x = left; x < width; x++) {
                byte color = getMapColor(left, top);
                colors[Framebuffer.index(x-left, y-top)] = color;
            }
        }

        packet.columns = (short) width;
        packet.rows = (short) height;
        packet.icons = new MapDataPacket.Icon[0];
        packet.x = 0;
        packet.z = 0;
        packet.data = colors;
    }
}
