package net.minestom.server.map;

import net.minestom.server.network.packet.server.play.MapDataPacket;

/**
 * Framebuffer to render to a map
 */
public interface Framebuffer {

    int WIDTH = 128;
    int HEIGHT = 128;

    byte[] toMapColors();

    default void preparePacket(MapDataPacket packet) {
        preparePacket(packet, 0, 0, WIDTH, HEIGHT);
    }

    default void preparePacket(MapDataPacket packet, int minX, int minY, int width, int height) {
        byte[] colors;
        if (minX == 0 && minY == 0 && width == WIDTH && height == HEIGHT) {
            colors = toMapColors();
        } else {
            colors = new byte[width * height];
            final byte[] mapColors = toMapColors();
            for (int y = minY; y < Math.min(HEIGHT, minY + height); y++) {
                for (int x = minX; x < Math.min(WIDTH, minX + width); x++) {
                    byte color = mapColors[index(x, y, WIDTH)];
                    colors[index(x - minX, y - minY, width)] = color;
                }
            }
        }

        packet.columns = (short) width;
        packet.rows = (short) height;
        packet.icons = new MapDataPacket.Icon[0];
        packet.x = (byte) minX;
        packet.z = (byte) minY;
        packet.data = colors;
    }

    static int index(int x, int z) {
        return index(x, z, WIDTH);
    }

    static int index(int x, int z, int stride) {
        return z * stride + x;
    }

}
