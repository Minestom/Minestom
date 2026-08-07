package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.Registries;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RegistriesTest
public class ItemRegistriesTest {
    @Test
    public void testFields() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        assertEquals(Material.DIAMOND_SWORD, item.material(), "Material must be the same");
        assertEquals(1, item.amount(), "Default item amount must be 1");

        // Should have the exact same components as the material prototype
        var prototype = Material.DIAMOND_SWORD.prototype();
        for (DataComponent<?> component : DataComponent.values()) {
            var proto = prototype.get(component);
            if (proto == null) {
                assertFalse(item.has(component), "Item should not have component " + component);
            } else {
                assertEquals(proto, item.get(component), "Item should have the same component as the prototype");
            }
        }

        ItemStack finalItem = item;
        assertThrows(UnsupportedOperationException.class, () -> finalItem.get(DataComponents.LORE).add(Component.text("Hey!")), "Lore list cannot be modified directly");

        item = item.withAmount(5);
        assertEquals(5, item.amount(), "Items with different amount should not be equals");
        assertEquals(10, item.withAmount(amount -> amount * 2).amount(), "Amount must be multiplied by 2");
    }

    @Test
    public void defaultBuilder() {
        var item = ItemStack.builder(Material.DIAMOND_SWORD).build();
        assertEquals(Material.DIAMOND_SWORD, item.material(), "Material must be the same");
        assertEquals(1, item.amount(), "Default item amount must be 1");

        // Should have the exact same components as the material prototype
        var prototype = Material.DIAMOND_SWORD.prototype();
        for (DataComponent<?> component : DataComponent.values()) {
            var proto = prototype.get(component);
            if (proto == null) {
                assertFalse(item.has(component), "Item should not have component " + component);
            } else {
                assertEquals(proto, item.get(component), "Item should have the same component as the prototype");
            }
        }

        ItemStack finalItem = item;
        assertThrows(UnsupportedOperationException.class, () -> finalItem.get(DataComponents.LORE).add(Component.text("Hey!")), "Lore list cannot be modified directly");

        item = item.withAmount(5);
        assertEquals(5, item.amount(), "Items with different amount should not be equals");
        assertEquals(10, item.withAmount(amount -> amount * 2).amount(), "Amount must be multiplied by 2");
    }

    @Test
    public void testEquality() {
        var item1 = ItemStack.of(Material.DIAMOND_SWORD);
        var item2 = ItemStack.of(Material.DIAMOND_SWORD);
        assertEquals(item1, item2);
        assertNotEquals(item1.withAmount(5), item2.withAmount(2));

        assertTrue(item1.isSimilar(item2));
        assertTrue(item1.withAmount(5).isSimilar(item2.withAmount(2)));
        assertFalse(item1.isSimilar(item2.with(DataComponents.CUSTOM_NAME, Component.text("Hey!"))));
    }

    @Test
    public void testEqualityComponents() {
        var item1 = ItemStack.of(Material.MUSIC_DISC_STAL);
        var item2 = ItemStack.of(Material.MUSIC_DISC_STAL).with(DataComponents.JUKEBOX_PLAYABLE, JukeboxSong.STAL);
        assertTrue(item1.isSimilar(item2));
    }

    @Test
    @SuppressWarnings("deprecation") // deliberately keeps coverage of the deprecated API until its removal
    public void testFromNbtLoreSpace(Registries registries) throws IOException {
        var itemStack = ItemStack.of(Material.LAPIS_BLOCK)
                .withLore(Component.text("Hey!", NamedTextColor.RED), Component.empty(), Component.text("hello"))
                .with(DataComponents.ITEM_MODEL, "unknown");
        var tagOut = MinestomAdventure.tagStringIO().asString(itemStack.toItemNBT(registries));
        var tagIn = MinestomAdventure.tagStringIO().asCompound(tagOut);
        assertEquals(itemStack, ItemStack.fromItemNBT(tagIn, registries));
    }

    @Test
    public void testImmutableLore() {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Hey!"));
        var itemStack = ItemStack.of(Material.LAPIS_BLOCK).withLore(lore);
        var itemStackLore = itemStack.get(DataComponents.LORE);
        assertNotNull(itemStackLore);
        assertEquals(lore, itemStackLore, "Lore list should have the same content");
        assertThrows(UnsupportedOperationException.class, () -> itemStackLore.add(Component.text("Hey!")), "Should be immutable");
    }

    @Test
    public void testBuilderImmutableLore() {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Hey!"));
        var itemStack = ItemStack.builder(Material.LAPIS_BLOCK).lore(lore).build();
        var itemStackLore = itemStack.get(DataComponents.LORE);
        assertNotNull(itemStackLore);
        assertEquals(lore, itemStackLore, "Lore list should have the same content");
        assertThrows(UnsupportedOperationException.class, () -> itemStackLore.add(Component.text("Hey!")), "Should be immutable");
    }

    @Test
    @SuppressWarnings("deprecation") // deliberately keeps coverage of the deprecated API until its removal
    public void testFromNbt(Registries registries) {
        var itemNbt = createItem().toItemNBT(registries);
        var item = ItemStack.fromItemNBT(itemNbt, registries);
        assertEquals(createItem(), item, "Items must be equal if created from the same item nbt");
        assertEquals(itemNbt, item.toItemNBT(registries), "Item nbt must be equal back");
    }

    @Test
    public void testBuilderReuse() {
        var builder = ItemStack.builder(Material.DIAMOND);
        var item1 = builder.build();
        var item2 = builder.set(DataComponents.CUSTOM_NAME, Component.text("Name")).build();
        assertNull(item1.get(DataComponents.CUSTOM_NAME));
        assertNotNull(item2.get(DataComponents.CUSTOM_NAME));
        assertNotEquals(item1, item2, "Item builder should be reusable");
    }

    @Test
    @SuppressWarnings("deprecation") // deliberately keeps coverage of the deprecated API until its removal
    public void materialUpdate(Registries registries) {
        var item1 = ItemStack.builder(Material.DIAMOND)
                .amount(5).set(DataComponents.CUSTOM_NAME, Component.text("Name"))
                .build();
        var item2 = item1.withMaterial(Material.GOLD_INGOT);

        assertEquals(Material.DIAMOND, item1.material());
        assertEquals(Material.GOLD_INGOT, item2.material());

        var nbt1 = item1.toItemNBT(registries).remove("id");
        var nbt2 = item2.toItemNBT(registries).remove("id");
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
    // Deliberately keeps coverage of the deprecated accessor until its removal
    @SuppressWarnings("removal")
    public void testEntityType() {
        var item1 = ItemStack.of(Material.DIAMOND, 1);
        assertNull(item1.get(DataComponents.ENTITY_DATA));
        var item2 = ItemStack.of(Material.CAMEL_SPAWN_EGG, 1);
        var entityData = item2.get(DataComponents.ENTITY_DATA);
        assertNotNull(entityData);
        assertEquals(EntityType.CAMEL, entityData.type());
    }

    @Test
    public void testModifyMaterialAmountNonzero() {
        var airItem = ItemStack.of(Material.AIR, 0);
        assertEquals(0, airItem.amount());
        var nonAirItem = airItem.withMaterial(Material.DIAMOND);
        assertEquals(1, nonAirItem.amount());
        var airAgainItem = nonAirItem.withMaterial(Material.AIR);
        assertEquals(0, airAgainItem.amount());
    }

    static ItemStack createItem() {
        return ItemStack.builder(Material.STONE)
                .set(DataComponents.CUSTOM_NAME, Component.text("Display name!", NamedTextColor.GREEN))
                .set(DataComponents.LORE, List.of(Component.text("Line 1"), Component.text("Line 2")))
                .set(DataComponents.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.EFFICIENCY, 10)))
                .build();
    }
}
