package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jglrxavpok.hephaistos.nbt.NBTString;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void testFields() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        assertEquals(item.material(), Material.DIAMOND_SWORD, "Material must be the same");
        assertEquals(item.amount(), 1, "Default item amount must be 1");
        assertNull(item.getDisplayName(), "Default item display name must be null");
        assertTrue(item.getLore().isEmpty(), "Default item lore must be empty");
        ItemStack finalItem = item;
        assertThrows(Exception.class, () -> finalItem.getLore().add(Component.text("Hey!")), "Lore list cannot be modified directly");

        item = item.withAmount(5);
        assertEquals(item.amount(), 5, "Items with different amount should not be equals");
        assertEquals(item.withAmount(amount -> amount * 2).amount(), 10, "Amount must be multiplied by 2");
    }

    @Test
    public void defaultBuilder() {
        var item = ItemStack.builder(Material.DIAMOND_SWORD).build();
        assertEquals(item.material(), Material.DIAMOND_SWORD, "Material must be the same");
        assertEquals(item.amount(), 1, "Default item amount must be 1");
        assertNull(item.getDisplayName(), "Default item display name must be null");
        assertTrue(item.getLore().isEmpty(), "Default item lore must be empty");
        ItemStack finalItem = item;
        assertThrows(Exception.class, () -> finalItem.getLore().add(Component.text("Hey!")), "Lore list cannot be modified directly");

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
        assertFalse(item1.isSimilar(item2.withDisplayName(Component.text("Hey!"))));
    }

    @Test
    public void testItemNbt() {
        var itemNbt = createItem().toItemNBT();
        assertEquals(itemNbt.getString("id"), createItem().material().name(), "id string should be the material name");
        assertEquals(itemNbt.getByte("Count"), (byte) createItem().amount(), "Count byte should be the item amount");
        var metaNbt = itemNbt.getCompound("tag");
        var metaNbt2 = createItem().meta().toNBT();
        assertEquals(metaNbt, metaNbt2, "tag compound should be equal to the meta nbt");
    }

    @Test
    public void testFromNbt() {
        var itemNbt = createItem().toItemNBT();
        var item = ItemStack.fromItemNBT(itemNbt);
        assertEquals(createItem(), item, "Items must be equal if created from the same item nbt");
        assertEquals(itemNbt, item.toItemNBT(), "Item nbt must be equal back");

        var metaNbt = createItem().meta().toNBT();
        item = ItemStack.fromNBT(createItem().material(), metaNbt, createItem().amount());
        assertEquals(createItem(), item, "Items must be equal if created from the same meta nbt");
    }

    @Test
    public void testEnchant() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        var enchantments = item.meta().getEnchantmentMap();
        assertTrue(enchantments.isEmpty(), "items do not have enchantments by default");

        item = item.withMeta(meta -> meta.enchantment(Enchantment.EFFICIENCY, (short) 10));
        enchantments = item.meta().getEnchantmentMap();
        assertEquals(enchantments.size(), 1);
        assertEquals(enchantments.get(Enchantment.EFFICIENCY), (short) 10);

        item = item.withMeta(meta -> meta.enchantment(Enchantment.INFINITY, (short) 5));
        enchantments = item.meta().getEnchantmentMap();
        assertEquals(enchantments.size(), 2);
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

    @Test
    public void testLore() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        assertEquals(List.of(), item.getLore());
        assertNull(item.meta().toNBT().get("display"));

        {
            var lore = List.of(Component.text("Hello"));
            item = item.withLore(lore);
            assertEquals(lore, item.getLore());
            var loreNbt = item.meta().toNBT().getCompound("display").<NBTString>getList("Lore");
            assertNotNull(loreNbt);
            assertEquals(loreNbt.getSize(), 1);
            assertEquals(lore, loreNbt.asListView().stream().map(line -> GsonComponentSerializer.gson().deserialize(line.getValue())).toList());
        }

        {
            var lore = List.of(Component.text("Hello"), Component.text("World"));
            item = item.withLore(lore);
            assertEquals(lore, item.getLore());
            var loreNbt = item.meta().toNBT().getCompound("display").<NBTString>getList("Lore");
            assertNotNull(loreNbt);
            assertEquals(loreNbt.getSize(), 2);
            assertEquals(lore, loreNbt.asListView().stream().map(line -> GsonComponentSerializer.gson().deserialize(line.getValue())).toList());
        }

        {
            var lore = Stream.of("string test").map(Component::text).toList();
            item = item.withLore(lore);
            assertEquals(lore, item.getLore());
            var loreNbt = item.meta().toNBT().getCompound("display").<NBTString>getList("Lore");
            assertNotNull(loreNbt);
            assertEquals(loreNbt.getSize(), 1);
            assertEquals(lore, loreNbt.asListView().stream().map(line -> GsonComponentSerializer.gson().deserialize(line.getValue())).toList());
        }

        // Ensure that lore can be properly removed without residual (display compound)
        item = item.withLore(List.of());
        assertNull(item.meta().toNBT().get("display"));
    }

    @Test
    public void testBuilderReuse() {
        var builder = ItemStack.builder(Material.DIAMOND);
        var item1 = builder.build();
        var item2 = builder.displayName(Component.text("Name")).build();
        assertNull(item1.getDisplayName());
        assertNotNull(item2.getDisplayName());
        assertNotEquals(item1, item2, "Item builder should be reusable");
    }

    static ItemStack createItem() {
        return ItemStack.builder(Material.STONE)
                .displayName(Component.text("Display name!", NamedTextColor.GREEN))
                .lore(Component.text("Line 1"), Component.text("Line 2"))
                .meta(metaBuilder ->
                        metaBuilder.enchantment(Enchantment.EFFICIENCY, (short) 10)
                                .hideFlag(ItemHideFlag.HIDE_ENCHANTS))
                .build();
    }
}
