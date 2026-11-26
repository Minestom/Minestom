package net.minestom.server.instance.block.transformer;

import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.world.generation.BlockStateProvider;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BlockTransformerTest {

    private static BlockTransformer.BlockTransformData transform(int itemDamagePerUse) {
        return new BlockTransformer.BlockTransformData(new BlockStateProvider.Simple(Block.DIRT_PATH),
                SoundEvent.INTENTIONALLY_EMPTY, BlockTransformer.TransformParticle.NONE, List.of(), null,
                BlockTransformer.DropStrategy.FROM_MIDDLE, true, BlockTransformer.TransformType.SINGLE_BLOCK, true,
                itemDamagePerUse);
    }

    @Test
    public void transformCount() {
        assertDoesNotThrow(() -> new BlockTransformer(List.of(transform(0))));
        assertDoesNotThrow(() -> new BlockTransformer(Collections.nCopies(200, transform(0))));
        assertThrows(IllegalArgumentException.class, () -> new BlockTransformer(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new BlockTransformer(Collections.nCopies(200 + 1, transform(0))));
    }

    @Test
    public void itemDamageMustNotBeNegative() {
        assertDoesNotThrow(() -> transform(1));
        assertThrows(IllegalArgumentException.class, () -> transform(-1));
    }
}
