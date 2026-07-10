package net.minestom.server.map;

import net.minestom.server.network.packet.server.play.MapDataPacket;

import java.util.List;

/**
 * Framebuffer to render to a map
 */
public interface Framebuffer {

    int WIDTH = 128;
    int HEIGHT = 128;

    byte[] toMapColors();

    default MapDataPacket preparePacket(int mapId) {
        return preparePacket(mapId, 0, 0, WIDTH, HEIGHT);
    }

    default MapDataPacket preparePacket(int mapId, int minX, int minY, int width, int height) {
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
        return new MapDataPacket(mapId, (byte) 0, false,
                false, List.of(),
                new MapDataPacket.ColorContent((byte) width, (byte) height,
                        (byte) minX, (byte) minY,
                        colors));
    }

    static int index(int x, int z) {
        return index(x, z, WIDTH);
    }

    static int index(int x, int z, int stride) {
        return z * stride + x;
    }

}
