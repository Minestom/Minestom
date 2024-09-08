package net.minestom.server.instance.painter;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

public class PainterTest {
    @Test
    public void empty() {
        Painter painter = Painter.paint(world -> {
        });
    }

    @Test
    public void singleBlock() {
        Painter painter = Painter.paint(world -> world.setBlock(Vec.ZERO, Block.STONE));
    }
}
