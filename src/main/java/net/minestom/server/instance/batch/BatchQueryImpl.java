package net.minestom.server.instance.batch;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

record BatchQueryImpl(int radius,
                      @NotNull Predicate<Block> predicate) implements BatchQuery {

    static Result fallback(Block.Getter getter, int x, int y, int z,
                           Block.Getter.Condition condition, BatchQuery query) {
        Map<Point, Block> blocks = new HashMap<>();

        BatchQueryImpl queryImpl = (BatchQueryImpl) query;
        final int radius = queryImpl.radius;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int k = -radius; k <= radius; k++) {
                    final int blockX = x + i;
                    final int blockY = y + j;
                    final int blockZ = z + k;
                    final Block block = getter.getBlock(blockX, blockY, blockZ);
                    if (!queryImpl.predicate.test(block)) {
                        continue;
                    }
                    blocks.put(new Vec(blockX, blockY, blockZ), block);
                }
            }
        }

        return new FallbackResult(blocks);
    }

    record FallbackResult(Map<Point, Block> blocks) implements Result {
        public FallbackResult {
            blocks = Map.copyOf(blocks);
        }

        @Override
        public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
            return blocks.get(new Vec(x, y, z));
        }

        @Override
        public int count() {
            return blocks.size();
        }
    }

    static final class Builder implements BatchQuery.Builder {
        private final int radius;
        private Set<Integer> type;
        private Set<Block> exact;

        Builder(Integer radius) {
            this.radius = radius;
        }

        @Override
        public BatchQuery.@NotNull Builder type(@NotNull Block @NotNull ... blocks) {
            this.type = Arrays.stream(blocks).map(Block::id).collect(Collectors.toUnmodifiableSet());
            return this;
        }

        @Override
        public BatchQuery.@NotNull Builder exact(@NotNull Block @NotNull ... blocks) {
            this.exact = Set.of(blocks);
            return this;
        }

        @Override
        public @NotNull BatchQuery build() {
            var type = this.type != null ? Set.copyOf(this.type) : null;
            var exact = this.exact != null ? Set.copyOf(this.exact) : null;
            return new BatchQueryImpl(radius,
                    block -> {
                        if (type != null && !type.contains(block.id()))
                            return false;
                        if (exact != null && !exact.contains(block))
                            return false;
                        return true;
                    });
        }
    }
}
