package net.minestom.server.world.generation;

import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Range;
import net.minestom.server.utils.WeightedList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BlockStateProviderTest {
    private static final NormalNoise NOISE = new NormalNoise(1.0, 0, 1, true, List.of());
    private static final List<Block> STATES = List.of(Block.STONE);

    @Test
    public void weightedNeedsEntries() {
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.Weighted(new WeightedList<>(List.of())));
        assertDoesNotThrow(() -> new BlockStateProvider.Weighted(WeightedList.of(new WeightedList.Entry<>(Block.STONE, 1))));
    }

    @Test
    public void noiseBounds() {
        assertDoesNotThrow(() -> new BlockStateProvider.Noise(1, NOISE, 0.5f, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.Noise(1, NOISE, 0f, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.Noise(1, NOISE, 0.5f, List.of()));
    }

    @Test
    public void noiseThresholdBounds() {
        assertDoesNotThrow(() -> new BlockStateProvider.NoiseThreshold(1, NOISE, 0.5f, -1f, 1f, Block.STONE, STATES, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.NoiseThreshold(1, NOISE, 0f, 0f, 0.5f, Block.STONE, STATES, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.NoiseThreshold(1, NOISE, 0.5f, 1.5f, 0.5f, Block.STONE, STATES, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.NoiseThreshold(1, NOISE, 0.5f, 0f, -0.1f, Block.STONE, STATES, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.NoiseThreshold(1, NOISE, 0.5f, 0f, 0.5f, Block.STONE, List.of(), STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.NoiseThreshold(1, NOISE, 0.5f, 0f, 0.5f, Block.STONE, STATES, List.of()));
    }

    @Test
    public void dualNoiseBounds() {
        assertDoesNotThrow(() -> new BlockStateProvider.DualNoise(new Range.Int(1, 64), NOISE, 1f, 1, NOISE, 1f, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.DualNoise(new Range.Int(0, 3), NOISE, 1f, 1, NOISE, 1f, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.DualNoise(new Range.Int(1, 65), NOISE, 1f, 1, NOISE, 1f, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.DualNoise(new Range.Int(null, 3), NOISE, 1f, 1, NOISE, 1f, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.DualNoise(new Range.Int(1, 3), NOISE, 0f, 1, NOISE, 1f, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.DualNoise(new Range.Int(1, 3), NOISE, 1f, 1, NOISE, 0f, STATES));
        assertThrows(IllegalArgumentException.class, () -> new BlockStateProvider.DualNoise(new Range.Int(1, 3), NOISE, 1f, 1, NOISE, 1f, List.of()));
    }
}
