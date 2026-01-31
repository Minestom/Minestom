package net.minestom.server.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DimensionTypeBuilderTest {
    @Test
    void testCoordinateScale() {
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().coordinateScale(0));
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().coordinateScale(30000001));

        assertDoesNotThrow(() -> DimensionType.builder().coordinateScale(1.5));
    }

    @Test
    void testMinY() {
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().minY(-2048));
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().minY(2032));
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().minY(7));

        assertDoesNotThrow(() -> DimensionType.builder().minY(-16));
    }

    @Test
    void testHeight() {
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().height(0));
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().height(4080));
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().height(17));

        assertDoesNotThrow(() -> DimensionType.builder().height(16));
    }

    @Test
    void testLogicalHeight() {
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().logicalHeight(-1));

        assertDoesNotThrow(() -> DimensionType.builder().logicalHeight(17));
    }

    @Test
    void testMonsterSpawnBlockLightLimit() {
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().monsterSpawnBlockLightLimit(-1));
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder().monsterSpawnBlockLightLimit(16));

        assertDoesNotThrow(() -> DimensionType.builder().monsterSpawnBlockLightLimit(15));
    }

    @Test
    void testBuild() {
        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder()
                .height(32)
                .logicalHeight(33)
                .build());

        assertThrows(IllegalArgumentException.class, () -> DimensionType.builder()
                .height(32)
                .minY(2016)
                .build());

        assertDoesNotThrow(() -> DimensionType.builder()
                .minY(2000)
                .height(32)
                .logicalHeight(10)
                .build());
    }
}