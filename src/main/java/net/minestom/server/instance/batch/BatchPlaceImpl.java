package net.minestom.server.instance.batch;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.UnitModifier;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


record BatchPlaceImpl(Point size,
                      Map<Point, Block> relativeBlocks,
                      Map<Point, Block> absoluteBlocks) implements BatchPlace {
    public BatchPlaceImpl {
        relativeBlocks = Map.copyOf(relativeBlocks);
        absoluteBlocks = Map.copyOf(absoluteBlocks);
    }

    static BatchPlace batch(Point size, Consumer<UnitModifier> consumer) {
        if (size.x() < 1 || size.y() < 1 || size.z() < 1) {
            throw new IllegalArgumentException("Size must be positive: " + size);
        }
        BatchModifier modifier = new BatchModifier(size);
        consumer.accept(modifier);
        return new BatchPlaceImpl(size, modifier.relativeBlocks, modifier.absoluteBlocks);
    }

    @Override
    public void forEachBlock(int originX, int originY, int originZ, @NotNull BiConsumer<@NotNull Block, @NotNull Point> consumer) {
        this.relativeBlocks.forEach((point, block) -> consumer.accept(block, point.add(originX, originY, originZ)));
        this.absoluteBlocks.forEach((point, block) -> {
            final int x = point.blockX();
            final int y = point.blockY();
            final int z = point.blockZ();
            if (x < 0 || y < 0 || z < 0 || x >= size.x()+originX || y >= size.y()+originY || z >= size.z()+originZ) {
                throw new IllegalArgumentException("Out of bounds: " + new Vec(x, y, z));
            }
            consumer.accept(block, point);
        });
    }

    private static final class BatchModifier implements UnitModifier {
        private final Point size;
        private final Map<Point, Block> relativeBlocks = new HashMap<>();
        private final Map<Point, Block> absoluteBlocks = new HashMap<>();

        private BatchModifier(Point size) {
            this.size = size;
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            this.absoluteBlocks.put(new Vec(x, y, z), block);
        }

        @Override
        public void setRelative(int x, int y, int z, @NotNull Block block) {
            if (x < 0 || y < 0 || z < 0 || x >= size.x() || y >= size.y() || z >= size.z()) {
                throw new IllegalArgumentException("Out of bounds: " + x + ", " + y + ", " + z);
            }
            this.relativeBlocks.put(new Vec(x, y, z), block);
        }

        @Override
        public void setAll(@NotNull Supplier supplier) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAllRelative(@NotNull Supplier supplier) {
            for (int x = 0; x < size.x(); x++) {
                for (int y = 0; y < size.y(); y++) {
                    for (int z = 0; z < size.z(); z++) {
                        this.relativeBlocks.put(new Vec(x, y, z), supplier.get(x, y, z));
                    }
                }
            }
        }

        @Override
        public void fill(@NotNull Block block) {
            for (int x = 0; x < size.x(); x++) {
                for (int y = 0; y < size.y(); y++) {
                    for (int z = 0; z < size.z(); z++) {
                        this.relativeBlocks.put(new Vec(x, y, z), block);
                    }
                }
            }
        }

        @Override
        public void fill(@NotNull Point start, @NotNull Point end, @NotNull Block block) {
            for(int x = start.blockX(); x < end.blockX(); x++) {
                for(int y = start.blockY(); y < end.blockY(); y++) {
                    for(int z = start.blockZ(); z < end.blockZ(); z++) {
                        this.absoluteBlocks.put(new Vec(x, y, z), block);
                    }
                }
            }
        }

        @Override
        public void fillHeight(int minHeight, int maxHeight, @NotNull Block block) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void fillBiome(@NotNull Biome biome) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setBiome(int x, int y, int z, @NotNull Biome biome) {
            throw new UnsupportedOperationException();
        }
    }
}
