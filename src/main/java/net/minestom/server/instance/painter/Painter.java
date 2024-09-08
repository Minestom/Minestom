package net.minestom.server.instance.painter;

import net.minestom.server.instance.block.Block;

import java.util.function.Consumer;

public interface Painter {
    static Painter paint(Consumer<World> consumer) {
        return new PainterImpl(consumer);
    }

    interface World extends Block.Getter, Block.Setter {
    }
}
