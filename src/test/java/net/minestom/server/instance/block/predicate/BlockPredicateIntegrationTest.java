package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.SuspiciousGravelBlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class BlockPredicateIntegrationTest {
    @Test
    public void testEmptySource(Env env) {
        var itemNbt = ItemStack.of(Material.STONE).toItemNBT(env.process());
        var predicate = new BlockPredicate(CompoundBinaryTag.builder()
                .putString("LootTable", "minecraft:test")
                .put("item", itemNbt)
                .build());
        var block = Block.SUSPICIOUS_GRAVEL
                .withHandler(SuspiciousGravelBlockHandler.INSTANCE)
                .withNbt(CompoundBinaryTag.builder()
                .putString("LootTable", "minecraft:test")
                .put("item", itemNbt)
                .build());
        assertTrue(predicate.test(block));
    }

    @Test
    public void testNoMatchDeep(Env env) {
        var itemNbt1 = ItemStack.of(Material.STONE).toItemNBT(env.process());
        var itemNbt2 = ItemStack.of(Material.STONE).withAmount(2).toItemNBT(env.process());
        var predicate = new BlockPredicate(CompoundBinaryTag.builder()
                .putString("LootTable", "minecraft:test")
                .put("item", itemNbt1)
                .build());
        var block = Block.SUSPICIOUS_GRAVEL
                .withHandler(SuspiciousGravelBlockHandler.INSTANCE)
                .withNbt(CompoundBinaryTag.builder()
                .putString("LootTable", "minecraft:test")
                .put("item", itemNbt2)
                .build());
        assertFalse(predicate.test(block));
    }
}
