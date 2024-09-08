package net.minestom.server.instance.painter;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.palette.Palette;

import java.util.function.Consumer;

public interface Painter {
    static Painter paint(Consumer<World> consumer) {
        return PainterImpl.paint(consumer);
    }

    Palette sectionAt(int sectionX, int sectionY, int sectionZ);

    default Generator asGenerator() {
        return unit -> {
            final Point start = unit.absoluteStart();
            final Point end = unit.absoluteEnd();
            final int minX = start.chunkX();
            final int minY = start.section();
            final int minZ = start.chunkZ();

            final int maxX = end.chunkX();
            final int maxY = end.section();
            final int maxZ = end.chunkZ();

            for (int sectionX = minX; sectionX < maxX; sectionX++) {
                for (int sectionY = minY; sectionY < maxY; sectionY++) {
                    for (int sectionZ = minZ; sectionZ < maxZ; sectionZ++) {
                        final Palette palette = sectionAt(sectionX, sectionY, sectionZ);
                        if (palette.count() == 0) continue;
                        final int finalSectionX = sectionX;
                        final int finalSectionY = sectionY;
                        final int finalSectionZ = sectionZ;
                        palette.getAllPresent((x, y, z, value) -> {
                            final int globalX = x + finalSectionX * 16;
                            final int globalY = y + finalSectionY * 16;
                            final int globalZ = z + finalSectionZ * 16;
                            final Block block = Block.fromStateId(value);
                            assert block != null;
                            unit.modifier().setBlock(globalX, globalY, globalZ, block);
                        });
                    }
                }
            }
        };
    }

    interface World extends Block.Getter, Block.Setter {
    }
}
