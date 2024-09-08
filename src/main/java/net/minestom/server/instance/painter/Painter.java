package net.minestom.server.instance.painter;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;

import java.util.function.Consumer;

public interface Painter {
    static Painter paint(Consumer<World> consumer) {
        return PainterImpl.paint(consumer);
    }

    Palette sectionAt(int x, int y, int z);

    interface World extends Block.Getter, Block.Setter {
    }
}
