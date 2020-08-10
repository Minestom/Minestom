package net.minestom.server.map.framebuffers;

import net.minestom.server.map.Framebuffer;
import net.minestom.server.map.LargeFramebuffer;
import net.minestom.server.map.MapColors;

public class LargeFramebufferDefaultView implements Framebuffer {
    private final LargeFramebuffer parent;
    private final int x;
    private final int y;
    private final byte[] colors = new byte[WIDTH*HEIGHT];

    public LargeFramebufferDefaultView(LargeFramebuffer parent, int x, int y) {
        this.parent = parent;
        this.x = x;
        this.y = y;
    }

    private boolean bounds(int x, int y) {
        return x >= 0 && x < parent.width() && y >= 0 && y < parent.height();
    }

    private byte colorOrNone(int x, int y) {
        if(!bounds(x, y)) return MapColors.NONE.baseColor();
        return parent.getMapColor(x, y);
    }

    @Override
    public byte[] toMapColors() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                colors[Framebuffer.index(x, y)] = colorOrNone(x+this.x, y+this.y);
            }
        }
        return colors;
    }
}
