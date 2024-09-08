package net.minestom.server.instance.painter;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

public class PainterTest {
    @Test
    public void empty() {
        Painter painter = Painter.paint(world -> {
        });
        painter.asGenerator();
    }

    @Test
    public void singleBlock() {
        Painter painter = Painter.paint(world -> world.setBlock(Vec.ZERO, Block.STONE));
    }

    @Test
    public void trees() {
        Painter painter = Painter.paint(world -> {

            // trees
            world.operation2d(WhiteNoise.noise(1.0 / (16 * 16), 42), (relWorld) -> {

                // log
                for (int i = 0; i < 10; i++) {
                    relWorld.setBlock(new Vec(0, i, 0), Block.OAK_LOG);
                }

                // leaves
                for (int x = -2; x <= 2; x++) {
                    for (int y = 5; y <= 10; y++) {
                        for (int z = -2; z <= 2; z++) {
                            if (Math.abs(x) + Math.abs(y - 7) + Math.abs(z) <= 4) {
                                relWorld.setBlock(new Vec(x, y, z), Block.OAK_LEAVES);
                            }
                        }
                    }
                }
            });
        });
    }
}
