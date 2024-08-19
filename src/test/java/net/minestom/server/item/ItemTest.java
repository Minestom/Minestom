package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
public class ItemTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testFields() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        assertEquals(item.material(), Material.DIAMOND_SWORD, "Material must be the same");
        assertEquals(item.amount(), 1, "Default item amount must be 1");

        // Should have the exact same components as the material prototype
        var prototype = Material.DIAMOND_SWORD.registry().prototype();
        for (DataComponent<?> component : ItemComponent.values()) {
            var proto = prototype.get(component);
            if (proto == null) {
                assertFalse(item.has(component), "Item should not have component " + component);
            } else {
                assertEquals(proto, item.get(component), "Item should have the same component as the prototype");
            }
        }

        ItemStack finalItem = item;
        assertThrows(UnsupportedOperationException.class, () -> finalItem.get(ItemComponent.LORE).add(Component.text("Hey!")), "Lore list cannot be modified directly");

        item = item.withAmount(5);
        assertEquals(item.amount(), 5, "Items with different amount should not be equals");
        assertEquals(item.withAmount(amount -> amount * 2).amount(), 10, "Amount must be multiplied by 2");
    }

    @Test
    public void defaultBuilder() {
        var item = ItemStack.builder(Material.DIAMOND_SWORD).build();
        assertEquals(item.material(), Material.DIAMOND_SWORD, "Material must be the same");
        assertEquals(item.amount(), 1, "Default item amount must be 1");

        // Should have the exact same components as the material prototype
        var prototype = Material.DIAMOND_SWORD.registry().prototype();
        for (DataComponent<?> component : ItemComponent.values()) {
            var proto = prototype.get(component);
            if (proto == null) {
                assertFalse(item.has(component), "Item should not have component " + component);
            } else {
                assertEquals(proto, item.get(component), "Item should have the same component as the prototype");
            }
        }

        ItemStack finalItem = item;
        assertThrows(UnsupportedOperationException.class, () -> finalItem.get(ItemComponent.LORE).add(Component.text("Hey!")), "Lore list cannot be modified directly");

        item = item.withAmount(5);
        assertEquals(item.amount(), 5, "Items with different amount should not be equals");
        assertEquals(item.withAmount(amount -> amount * 2).amount(), 10, "Amount must be multiplied by 2");
    }

    @Test
    public void testEquality() {
        var item1 = ItemStack.of(Material.DIAMOND_SWORD);
        var item2 = ItemStack.of(Material.DIAMOND_SWORD);
        assertEquals(item1, item2);
        assertNotEquals(item1.withAmount(5), item2.withAmount(2));

        assertTrue(item1.isSimilar(item2));
        assertTrue(item1.withAmount(5).isSimilar(item2.withAmount(2)));
        assertFalse(item1.isSimilar(item2.with(ItemComponent.CUSTOM_NAME, Component.text("Hey!"))));
    }

    @Test
    public void testFromNbt(Env env) {
        var itemNbt = createItem().toItemNBT();
        var item = ItemStack.fromItemNBT(itemNbt);
        assertEquals(createItem(), item, "Items must be equal if created from the same item nbt");
        assertEquals(itemNbt, item.toItemNBT(), "Item nbt must be equal back");
    }

    @Test
    public void testBuilderReuse() {
        var builder = ItemStack.builder(Material.DIAMOND);
        var item1 = builder.build();
        var item2 = builder.set(ItemComponent.CUSTOM_NAME, Component.text("Name")).build();
        assertNull(item1.get(ItemComponent.CUSTOM_NAME));
        assertNotNull(item2.get(ItemComponent.CUSTOM_NAME));
        assertNotEquals(item1, item2, "Item builder should be reusable");
    }

    @Test
    public void materialUpdate() {
        var item1 = ItemStack.builder(Material.DIAMOND)
                .amount(5).set(ItemComponent.CUSTOM_NAME, Component.text("Name"))
                .build();
        var item2 = item1.withMaterial(Material.GOLD_INGOT);

        assertEquals(Material.DIAMOND, item1.material());
        assertEquals(Material.GOLD_INGOT, item2.material());

        var nbt1 = item1.toItemNBT().remove("id");
        var nbt2 = item2.toItemNBT().remove("id");
        assertEquals(nbt1, nbt2);

        assertEquals(5, item1.amount());
        assertEquals(5, item2.amount());
    }

    @Test
    public void amountUpdate() {
        var item1 = ItemStack.of(Material.DIAMOND, 5);
        assertEquals(5, item1.amount());
        assertEquals(6, item1.withAmount(6).amount());
    }

    @Test
    public void testEntityType() {
        var item1 = ItemStack.of(Material.DIAMOND, 1);
        assertNull(item1.material().registry().spawnEntityType());
        var item2 = ItemStack.of(Material.CAMEL_SPAWN_EGG, 1);
        assertNotNull(item2.material().registry().spawnEntityType());
        assertEquals(EntityType.CAMEL, item2.material().registry().spawnEntityType());
    }

    static ItemStack createItem() {
        return ItemStack.builder(Material.STONE)
                .set(ItemComponent.CUSTOM_NAME, Component.text("Display name!", NamedTextColor.GREEN))
                .set(ItemComponent.LORE, List.of(Component.text("Line 1"), Component.text("Line 2")))
                .set(ItemComponent.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.EFFICIENCY, 10), false))
                .build();
    }
}
