package net.minestom.server.instance.batch;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

record BatchQueryImpl(int radius,
                      @Nullable Set<Integer> type,
                      @Nullable Set<Block> exact) implements BatchQuery {
    public BatchQueryImpl {
        type = type != null ? Set.copyOf(type) : null;
        exact = exact != null ? Set.copyOf(exact) : null;
    }

    static BatchQuery radius(int radius) {
        return new BatchQueryImpl(radius, null, null);
    }

    @Override
    public @NotNull BatchQuery withType(@NotNull Block @NotNull ... blocks) {
        var updatedType = Arrays.stream(blocks).map(Block::id).collect(Collectors.toUnmodifiableSet());
        return new BatchQueryImpl(radius, updatedType, exact);
    }

    @Override
    public @NotNull BatchQuery withExact(@NotNull Block @NotNull ... blocks) {
        return new BatchQueryImpl(radius, type, Set.of(blocks));
    }

    private boolean valid(Block block) {
        if (type != null && !type.contains(block.id()))
            return false;
        return exact == null || exact.contains(block);
    }

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
                    if (!queryImpl.valid(block)) {
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
}
