package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
    public void testBuilderReuse() {
        var builder = ItemStack.builder(Material.DIAMOND);
        var item1 = builder.build();
        var item2 = builder.displayName(Component.text("Name")).build();
        assertNull(item1.getDisplayName());
        assertNotNull(item2.getDisplayName());
        assertNotEquals(item1, item2, "Item builder should be reusable");
    }

    @Test
    public void materialUpdate() {
        var nbt = NBT.Compound(Map.of("key", NBT.String("value")));
        var item1 = ItemStack.fromNBT(Material.DIAMOND, nbt, 5);
        var item2 = item1.withMaterial(Material.GOLD_INGOT);

        assertEquals(Material.DIAMOND, item1.material());
        assertEquals(Material.GOLD_INGOT, item2.material());

        assertEquals(nbt, item1.meta().toNBT());
        assertEquals(nbt, item2.meta().toNBT());

        assertEquals(5, item1.amount());
        assertEquals(5, item2.amount());
    }

    @Test
    public void amountUpdate() {
        var item1 = ItemStack.of(Material.DIAMOND, 5);
        assertEquals(5, item1.amount());
        assertEquals(6, item1.withAmount(6).amount());
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
