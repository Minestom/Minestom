package net.minestom.server.collision;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestShape {

    private static @NotNull Stream<Arguments> isFullFaceCases() {
        return Stream.of(
                Arguments.of(Block.STONE, BlockFace.BOTTOM, true),
                Arguments.of(Block.ENCHANTING_TABLE, BlockFace.BOTTOM, true),
                Arguments.of(Block.ENCHANTING_TABLE, BlockFace.TOP, false),
                Arguments.of(Block.ENCHANTING_TABLE, BlockFace.NORTH, false),
                Arguments.of(Block.ACACIA_FENCE, BlockFace.TOP, false),
                Arguments.of(Block.IRON_BARS, BlockFace.TOP, false)
        );
    }

    @ParameterizedTest
    @MethodSource("isFullFaceCases")
    void isFullFace(@NotNull Block block, @NotNull BlockFace face, boolean isFullFace) {
        assertEquals(block.registry().collisionShape().isFaceFull(face), isFullFace);
    }
}
