package net.minestom.server.item;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class ItemAirTest {
    @Test
    public void testAir(Env ignored) {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        assertFalse(item.isAir());
        assertTrue(ItemStack.AIR.isAir());
        var emptyItem = item.withAmount(0);
        assertTrue(emptyItem.isAir());
        assertEquals(emptyItem, ItemStack.AIR, "AIR item can be compared to empty item");
        assertSame(emptyItem, ItemStack.AIR, "AIR item identity can be compared to empty item");

        assertSame(ItemStack.AIR, ItemStack.of(Material.DIAMOND, 0));
        assertSame(ItemStack.AIR, ItemStack.builder(Material.DIAMOND).amount(0).build());
    }
}
