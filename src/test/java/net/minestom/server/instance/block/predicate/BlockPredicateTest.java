package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.SuspiciousGravelBlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockPredicateTest {

    static {
        MinecraftServer.init();
    }

    // See sibling files for blocks and properties tests

    @Nested
    class NbtPredicate {
        private static final Block SUS_GRAVEL = Block.SUSPICIOUS_GRAVEL.withHandler(SuspiciousGravelBlockHandler.INSTANCE);

        @Test
        public void testMatching() {
            var predicate = new BlockPredicate(CompoundBinaryTag.builder()
                    .putString("LootTable", "minecraft:test")
                    .build());
            var block = SUS_GRAVEL.withNbt(CompoundBinaryTag.builder()
                    .putString("LootTable", "minecraft:test")
                    .build());
            assertTrue(predicate.test(block));
        }

        @Test
        public void testEmptyTarget() {
            var predicate = new BlockPredicate(CompoundBinaryTag.builder()
                    .putString("LootTable", "minecraft:test")
                    .build());
            var block = SUS_GRAVEL.withNbt(CompoundBinaryTag.builder()
                    .build());
            assertFalse(predicate.test(block));
        }

        @Test
        public void testEmptySource() {
            var itemNbt = ItemStack.of(Material.STONE).toItemNBT();
            var predicate = new BlockPredicate(CompoundBinaryTag.builder()
                    .putString("LootTable", "minecraft:test")
                    .put("item", itemNbt)
                    .build());
            var block = SUS_GRAVEL.withNbt(CompoundBinaryTag.builder()
                    .putString("LootTable", "minecraft:test")
                    .put("item", itemNbt)
                    .build());
            assertTrue(predicate.test(block));
        }

        @Test
        public void testNoMatchDeep() {
            var itemNbt1 = ItemStack.of(Material.STONE).toItemNBT();
            var itemNbt2 = ItemStack.of(Material.STONE).withAmount(2).toItemNBT();
            var predicate = new BlockPredicate(CompoundBinaryTag.builder()
                    .putString("LootTable", "minecraft:test")
                    .put("item", itemNbt1)
                    .build());
            var block = SUS_GRAVEL.withNbt(CompoundBinaryTag.builder()
                    .putString("LootTable", "minecraft:test")
                    .put("item", itemNbt2)
                    .build());
            assertFalse(predicate.test(block));
        }

        @Test
        public void testNoBlockEntity() {
            // Never match if the block has no client block entity

            var predicate = new BlockPredicate(CompoundBinaryTag.builder().build());
            var block = Block.STONE;
            assertFalse(predicate.test(block), "stone should not match empty");
        }

        @Test
        public void testNoExposedTags() {
            var predicate = new BlockPredicate(CompoundBinaryTag.builder().putString("LootTable", "minecraft:stone").build());
            // No exposed tags because no block handler so cannot match
            assertFalse(predicate.test(Block.SUSPICIOUS_GRAVEL.withHandler(SuspiciousGravelBlockHandler.INSTANCE_NO_TAGS)
                    .withNbt(CompoundBinaryTag.builder().putString("LootTable", "minecraft:stone").build())));

            // In this case its fine because when there is no block handler we send the entire block entity
            assertTrue(predicate.test(Block.SUSPICIOUS_GRAVEL.withNbt(CompoundBinaryTag.builder().putString("LootTable", "minecraft:stone").build())));
        }
    }


    // Combinations

    @Test
    public void emptyMatchAnything() {
        var predicate = new BlockPredicate(null, null, null);
        assertTrue(predicate.test(Block.STONE_STAIRS));
        assertTrue(predicate.test(Block.STONE_STAIRS.withProperty("facing", "east")));
        assertTrue(predicate.test(Block.SUSPICIOUS_GRAVEL.withHandler(SuspiciousGravelBlockHandler.INSTANCE)));
        assertTrue(predicate.test(Block.SUSPICIOUS_GRAVEL.withNbt(CompoundBinaryTag.builder().build())));
        assertTrue(predicate.test(Block.SUSPICIOUS_GRAVEL.withNbt(CompoundBinaryTag.builder().putString("LootTable", "minecraft:test").build())));
        assertTrue(predicate.test(Block.SUSPICIOUS_GRAVEL.withHandler(SuspiciousGravelBlockHandler.INSTANCE)
                .withNbt(CompoundBinaryTag.builder().putString("LootTable", "minecraft:test").build())));
    }

    @Test
    public void blockAlone() {
        var predicate = new BlockPredicate(new BlockTypeFilter.Blocks(Block.STONE));
        assertTrue(predicate.test(Block.STONE));
        assertFalse(predicate.test(Block.DIRT));
    }

    @Test
    public void propsAlone() {
        var predicate = new BlockPredicate(PropertiesPredicate.exact("facing", "east"));
        assertTrue(predicate.test(Block.STONE_STAIRS.withProperty("facing", "east")));
        assertTrue(predicate.test(Block.FURNACE.withProperty("facing", "east")));
        assertFalse(predicate.test(Block.FURNACE));
    }

    @Test
    public void nbtAlone() {
        var predicate = new BlockPredicate(CompoundBinaryTag.builder().putString("LootTable", "minecraft:stone").build());
        assertTrue(predicate.test(Block.SUSPICIOUS_GRAVEL.withHandler(SuspiciousGravelBlockHandler.INSTANCE)
                .withNbt(CompoundBinaryTag.builder().putString("LootTable", "minecraft:stone").build())));
    }
}
