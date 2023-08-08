package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {

    static {
        // Required to prevent initialization error during event call
        MinecraftServer.init();
    }

    @Test
    public void testCreation() {
        ContainerInventory inventory = new ContainerInventory(InventoryType.CHEST_1_ROW, "title");
        assertEquals(InventoryType.CHEST_1_ROW, inventory.getInventoryType());
        assertEquals(Component.text("title"), inventory.getTitle());

        inventory.setTitle(Component.text("new title"));
        assertEquals(Component.text("new title"), inventory.getTitle());
    }

    @Test
    public void testEntry() {
        var item1 = ItemStack.of(Material.DIAMOND);
        var item2 = ItemStack.of(Material.GOLD_INGOT);

        ContainerInventory inventory = new ContainerInventory(InventoryType.CHEST_1_ROW, "title");
        assertSame(ItemStack.AIR, inventory.getItemStack(0));
        inventory.setItemStack(0, item1);
        assertSame(item1, inventory.getItemStack(0));

        inventory.setItemStack(0, ItemStack.AIR);
        assertSame(ItemStack.AIR, inventory.getItemStack(0));

        // Replace test
        inventory.replaceItemStack(0, itemStack -> {
            assertSame(ItemStack.AIR, itemStack);
            return item2;
        });
        assertSame(item2, inventory.getItemStack(0));
        inventory.replaceItemStack(0, itemStack -> {
            assertSame(item2, itemStack);
            return item1;
        });
        assertSame(item1, inventory.getItemStack(0));
    }

    @Test
    public void testTake() {
        ItemStack item = ItemStack.of(Material.DIAMOND, 32);
        ContainerInventory inventory = new ContainerInventory(InventoryType.CHEST_1_ROW, "title");
        inventory.setItemStack(0, item);
        assertTrue(inventory.takeItemStack(item, TransactionOption.DRY_RUN));
        assertTrue(inventory.takeItemStack(item.withAmount(31), TransactionOption.DRY_RUN));
        assertFalse(inventory.takeItemStack(item.withAmount(33), TransactionOption.DRY_RUN));

        inventory.setItemStack(1, item.withAmount(2));
        assertTrue(inventory.takeItemStack(item.withAmount(33), TransactionOption.DRY_RUN));
        assertTrue(inventory.takeItemStack(item.withAmount(34), TransactionOption.DRY_RUN));
    }

    @Test
    public void testAdd() {
        ContainerInventory inventory = new ContainerInventory(InventoryType.HOPPER, "title");
        assertTrue(inventory.addItemStack(ItemStack.of(Material.DIAMOND, 32), TransactionOption.ALL_OR_NOTHING));
        assertTrue(inventory.addItemStack(ItemStack.of(Material.GOLD_BLOCK, 32), TransactionOption.ALL_OR_NOTHING));
        assertTrue(inventory.addItemStack(ItemStack.of(Material.MAP, 32), TransactionOption.ALL_OR_NOTHING));
        assertTrue(inventory.addItemStack(ItemStack.of(Material.ANDESITE_WALL, 32), TransactionOption.ALL_OR_NOTHING));
        assertTrue(inventory.addItemStack(ItemStack.of(Material.ANDESITE, 32), TransactionOption.ALL_OR_NOTHING));
        assertFalse(inventory.addItemStack(ItemStack.of(Material.BLUE_CONCRETE, 32), TransactionOption.ALL_OR_NOTHING));
    }

    @Test
    public void testIds() {
        for (int i = 0; i <= 1000; ++i) {
            final byte windowId = new ContainerInventory(InventoryType.CHEST_1_ROW, "title").getWindowId();
            assertTrue(windowId > 0);
        }
    }
}
