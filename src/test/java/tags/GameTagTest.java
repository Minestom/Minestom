package tags;

import java.lang.reflect.Field;

import java.util.List;

import net.minestom.server.instance.block.Block;
import net.minestom.server.tags.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class GameTagTest {

    @Test
    void ensureNoConstantsAreNull() {
        checkConstants(BlockGameTags.class);
        checkConstants(EntityTypeGameTags.class);
        checkConstants(FluidGameTags.class);
        checkConstants(GameEventGameTags.class);
        checkConstants(ItemGameTags.class);
    }

    @Test
    void testEquality() {
        final var tag = new GameTag<>(Block.AIR.namespace(), GameTagType.BLOCKS, List.of(Block.AIR));
        assertEquals(tag, new GameTag<>(Block.AIR.namespace(), GameTagType.BLOCKS, List.of(Block.AIR)));
        assertNotEquals(tag, new GameTag<>(Block.STONE.namespace(), GameTagType.BLOCKS, List.of(Block.AIR)));
        assertNotEquals(tag, new GameTag<>(Block.AIR.namespace(), GameTagType.BLOCKS, List.of()));
    }

    @Test
    void testRetrieval() {
        assertSame(BlockGameTags.ACACIA_LOGS, GameTags.get(GameTagType.BLOCKS, BlockGameTags.ACACIA_LOGS.name().asString()));
        assertSame(EntityTypeGameTags.ARROWS, GameTags.get(GameTagType.ENTITY_TYPES, EntityTypeGameTags.ARROWS.name().asString()));
        assertSame(FluidGameTags.LAVA, GameTags.get(GameTagType.FLUIDS, FluidGameTags.LAVA.name().asString()));
        assertSame(ItemGameTags.ACACIA_LOGS, GameTags.get(GameTagType.ITEMS, ItemGameTags.ACACIA_LOGS.name().asString()));
        assertSame(GameEventGameTags.VIBRATIONS, GameTags.get(GameTagType.GAME_EVENTS, GameEventGameTags.VIBRATIONS.name().asString()));
    }

    private static void checkConstants(final Class<?> clazz) {
        for (final Field field : clazz.getDeclaredFields()) {
            assertNotNull(assertDoesNotThrow(() -> field.get(null)));
        }
    }
}
