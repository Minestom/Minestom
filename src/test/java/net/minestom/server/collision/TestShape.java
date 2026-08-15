package net.minestom.server.collision;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestShape {

    private static Stream<Arguments> isFullFaceCases() {
        return Stream.of(
                Arguments.of(Block.STONE, BlockFace.BOTTOM, true),
                Arguments.of(Block.ENCHANTING_TABLE, BlockFace.BOTTOM, true),
                Arguments.of(Block.ENCHANTING_TABLE, BlockFace.TOP, false),
                Arguments.of(Block.ENCHANTING_TABLE, BlockFace.NORTH, false),
                Arguments.of(Block.ACACIA_FENCE, BlockFace.TOP, false),
                Arguments.of(Block.IRON_BARS, BlockFace.TOP, false),
                // We are testing collision faces here, so this should be true even though it doesnt occlude light
                Arguments.of(Block.GLASS, BlockFace.TOP, true),
                Arguments.of(Block.DARK_OAK_DOOR, BlockFace.NORTH, false),
                Arguments.of(Block.DARK_OAK_DOOR, BlockFace.SOUTH, true)
        );
    }

    @ParameterizedTest
    @MethodSource("isFullFaceCases")
    void isFullFace(Block block, BlockFace face, boolean isFullFace) {
        assertEquals(block.collisionShape().isFaceFull(face), isFullFace);
    }

    @Test
    void negativeCoordinates() {
        // Pitcher crop's registry collision shape is [AABB[0.3125, -0.0625, 0.3125] -> [0.6875, 0.1875, 0.6875]],
        // the sign of the negative Y coordinate must survive parsing
        Shape shape = Block.PITCHER_CROP.collisionShape();
        assertEquals(new Vec(0.3125, -0.0625, 0.3125), shape.relativeStart());
        assertEquals(new Vec(0.6875, 0.1875, 0.6875), shape.relativeEnd());
    }
}
