package inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class InventoryTest {

    @Test
    public void testCreation() {
        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "title");
        assertEquals(InventoryType.CHEST_1_ROW, inventory.getInventoryType());
        assertEquals(Component.text("title"), inventory.getTitle());
    }

    @Test
    public void testEntry() {
        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "title");
        assertSame(ItemStack.AIR, inventory.getItemStack(0));
        var item = ItemStack.of(Material.DIAMOND);
        inventory.setItemStack(0, item);
        assertSame(item, inventory.getItemStack(0));

        inventory.setItemStack(0, ItemStack.AIR);
        assertSame(ItemStack.AIR, inventory.getItemStack(0));
    }
}
