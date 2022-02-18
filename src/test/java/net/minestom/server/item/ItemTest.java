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
    private static final ItemStack ITEM = ItemStack.builder(Material.STONE)
            .displayName(Component.text("Display name!", NamedTextColor.GREEN))
            .lore(Component.text("Line 1"), Component.text("Line 2"))
            .meta(metaBuilder ->
                    metaBuilder.enchantment(Enchantment.EFFICIENCY, (short) 10)
                            .hideFlag(ItemHideFlag.HIDE_ENCHANTS))
            .build();

    @Test
    public void testFields() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        assertEquals(item.getMaterial(), Material.DIAMOND_SWORD, "Material must be the same");
        assertEquals(item.getAmount(), 1, "Default item amount must be 1");
        assertNull(item.getDisplayName(), "Default item display name must be null");
        assertTrue(item.getLore().isEmpty(), "Default item lore must be empty");
        ItemStack finalItem = item;
        assertThrows(Exception.class, () -> finalItem.getLore().add(Component.text("Hey!")), "Lore list cannot be modified directly");

        item = item.withAmount(5);
        assertEquals(item.getAmount(), 5, "Items with different amount should not be equals");
        assertEquals(item.withAmount(amount -> amount * 2).getAmount(), 10, "Amount must be multiplied by 2");
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
    public void testAir() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        assertFalse(item.isAir());
        assertTrue(ItemStack.AIR.isAir());
        var emptyItem = item.withAmount(0);
        assertTrue(emptyItem.isAir());
        assertEquals(emptyItem, ItemStack.AIR, "AIR item can be compared to empty item");
        assertSame(emptyItem, ItemStack.AIR, "AIR item identity can be compared to empty item");
    }

    @Test
    public void testItemNbt() {
        var itemNbt = ITEM.toItemNBT();
        assertEquals(itemNbt.getString("id"), ITEM.getMaterial().name(), "id string should be the material name");
        assertEquals(itemNbt.getByte("Count"), (byte) ITEM.getAmount(), "Count byte should be the item amount");
        var metaNbt = itemNbt.getCompound("tag");
        var metaNbt2 = ITEM.getMeta().toNBT();
        assertEquals(metaNbt, metaNbt2, "tag compound should be equal to the meta nbt");
    }

    @Test
    public void testFromNbt() {
        var itemNbt = ITEM.toItemNBT();
        var item = ItemStack.fromItemNBT(itemNbt);
        assertEquals(ITEM, item, "Items must be equal if created from the same item nbt");
        assertEquals(itemNbt, item.toItemNBT(), "Item nbt must be equal back");

        var metaNbt = ITEM.getMeta().toNBT();
        item = ItemStack.fromNBT(ITEM.getMaterial(), metaNbt, ITEM.getAmount());
        assertEquals(ITEM, item, "Items must be equal if created from the same meta nbt");
    }

    @Test
    public void testEnchant() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        var enchantments = item.getMeta().getEnchantmentMap();
        assertTrue(enchantments.isEmpty(), "items do not have enchantments by default");

        item = item.withMeta(meta -> meta.enchantment(Enchantment.EFFICIENCY, (short) 10));
        enchantments = item.getMeta().getEnchantmentMap();
        assertEquals(enchantments.size(), 1);
        assertEquals(enchantments.get(Enchantment.EFFICIENCY), (short) 10);

        item = item.withMeta(meta -> meta.enchantment(Enchantment.INFINITY, (short) 5));
        enchantments = item.getMeta().getEnchantmentMap();
        assertEquals(enchantments.size(), 2);
        assertEquals(enchantments.get(Enchantment.EFFICIENCY), (short) 10);
        assertEquals(enchantments.get(Enchantment.INFINITY), (short) 5);

        item = item.withMeta(meta -> meta.enchantments(Map.of()));
        enchantments = item.getMeta().getEnchantmentMap();
        assertTrue(enchantments.isEmpty());

        // Ensure that enchantments can still be modified after being emptied
        item = item.withMeta(meta -> meta.enchantment(Enchantment.EFFICIENCY, (short) 10));
        enchantments = item.getMeta().getEnchantmentMap();
        assertEquals(enchantments.get(Enchantment.EFFICIENCY), (short) 10);

        item = item.withMeta(ItemMetaBuilder::clearEnchantment);
        enchantments = item.getMeta().getEnchantmentMap();
        assertTrue(enchantments.isEmpty());
    }

    @Test
    public void testLore() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        assertEquals(List.of(), item.getLore());
        assertNull(item.getMeta().toNBT().get("display"));

        {
            var lore = List.of(Component.text("Hello"));
            item = item.withLore(lore);
            assertEquals(lore, item.getLore());
            var loreNbt = item.getMeta().toNBT().getCompound("display").<NBTString>getList("Lore");
            assertNotNull(loreNbt);
            assertEquals(loreNbt.getSize(), 1);
            assertEquals(lore, loreNbt.asListView().stream().map(line -> GsonComponentSerializer.gson().deserialize(line.getValue())).toList());
        }

        {
            var lore = List.of(Component.text("Hello"), Component.text("World"));
            item = item.withLore(lore);
            assertEquals(lore, item.getLore());
            var loreNbt = item.getMeta().toNBT().getCompound("display").<NBTString>getList("Lore");
            assertNotNull(loreNbt);
            assertEquals(loreNbt.getSize(), 2);
            assertEquals(lore, loreNbt.asListView().stream().map(line -> GsonComponentSerializer.gson().deserialize(line.getValue())).toList());
        }

        {
            var lore = Stream.of("string test").map(Component::text).toList();
            item = item.withLore(lore);
            assertEquals(lore, item.getLore());
            var loreNbt = item.getMeta().toNBT().getCompound("display").<NBTString>getList("Lore");
            assertNotNull(loreNbt);
            assertEquals(loreNbt.getSize(), 1);
            assertEquals(lore, loreNbt.asListView().stream().map(line -> GsonComponentSerializer.gson().deserialize(line.getValue())).toList());
        }

        // Ensure that lore can be properly removed without residual (display compound)
        item = item.withLore(List.of());
        assertNull(item.getMeta().toNBT().get("display"));
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
}
