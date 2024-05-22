package net.minestom.server.item;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemEnchantTest {

    @Test
    void enchant() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        var enchantments = item.meta().getEnchantmentMap();
        assertTrue(enchantments.isEmpty(), "items do not have enchantments by default");

        item = item.withMeta(meta -> meta.enchantment(Enchantment.EFFICIENCY, (short) 10));
        enchantments = item.meta().getEnchantmentMap();
        assertEquals(1, enchantments.size());
        assertEquals(enchantments.get(Enchantment.EFFICIENCY), (short) 10);

        item = item.withMeta(meta -> meta.enchantment(Enchantment.INFINITY, (short) 5));
        enchantments = item.meta().getEnchantmentMap();
        assertEquals(2, enchantments.size());
        assertEquals(enchantments.get(Enchantment.EFFICIENCY), (short) 10);
        assertEquals(enchantments.get(Enchantment.INFINITY), (short) 5);

        item = item.withMeta(meta -> meta.enchantments(Map.of()));
        enchantments = item.meta().getEnchantmentMap();
        assertTrue(enchantments.isEmpty());

        // Ensure that enchantments can still be modified after being emptied
        item = item.withMeta(meta -> meta.enchantment(Enchantment.EFFICIENCY, (short) 10));
        enchantments = item.meta().getEnchantmentMap();
        assertEquals(enchantments.get(Enchantment.EFFICIENCY), (short) 10);

        item = item.withMeta(ItemMeta.Builder::clearEnchantment);
        enchantments = item.meta().getEnchantmentMap();
        assertTrue(enchantments.isEmpty());
    }
}
