package net.minestom.server.world.generator;

import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.WeightedRandom;
import net.minestom.server.utils.WeightedRandomItem;
import net.minestom.server.utils.math.IntRange;
import net.minestom.server.utils.noise.Noise3D;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Used for generating blocks of chunks
 */
public class BlockPool implements BlockProvider {
    private final Noise3D valueNoise;
    private final Set<BlockPoolEntry> blockPoolEntrySet;

    public BlockPool(Noise3D valueNoise) {
        this.valueNoise = valueNoise;
        this.blockPoolEntrySet = new HashSet<>();
    }

    /**
     * Adds a block to this pool
     * @param block block to add
     * @param weight chance of generating this block
     * @param level limit the occurrence of the block between a specified level relative to the surface<br>
     *              i.e. 0-0 means top layer (e.g. grass), (-1)-(-3) means 3 layers below top (e.g. dirt)
     */
    public void addBlock(Block block, float weight, IntRange level) {
        blockPoolEntrySet.add(new BlockPoolEntry(block, weight, level));
    }

    /**
     * Gets a block based on the provided noise function
     * @param x global X coordinate (has no effect if the noise function is random)
     * @param y global Y coordinate
     * @param z global Z coordinate (has no effect if the noise function is random)
     * @param heightXZ surface height at X;Z
     * @return a block from the pool satisfying level requirements
     */
    @Override
    public Block getBlock(int x, int y, int z, int heightXZ) {
        final int relativeLevel = y - heightXZ;
        return new WeightedRandom<>(blockPoolEntrySet.stream().filter(b -> b.level.isInRange(relativeLevel)).collect(Collectors.toSet()))
                .get(valueNoise.getValue(x, y, z)).block;
    }

    private record BlockPoolEntry(Block block, float weight, IntRange level) implements WeightedRandomItem {
        @Override
        public double getWeight() {
            return weight;
        }
    }
}
