package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jglrxavpok.hephaistos.nbt.NBTString;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ItemDisplayTest {

    @Test
    public void lore() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        assertEquals(List.of(), item.getLore());
        assertNull(item.meta().toNBT().get("display"));

        {
            var lore = List.of(Component.text("Hello"));
            item = item.withLore(lore);
            assertEquals(lore, item.getLore());
            var loreNbt = item.meta().toNBT().getCompound("display").<NBTString>getList("Lore");
            assertNotNull(loreNbt);
            assertEquals(1, loreNbt.getSize());
            assertEquals(lore, loreNbt.asListView().stream().map(line -> GsonComponentSerializer.gson().deserialize(line.getValue())).toList());
        }

        {
            var lore = List.of(Component.text("Hello"), Component.text("World"));
            item = item.withLore(lore);
            assertEquals(lore, item.getLore());
            var loreNbt = item.meta().toNBT().getCompound("display").<NBTString>getList("Lore");
            assertNotNull(loreNbt);
            assertEquals(2, loreNbt.getSize());
            assertEquals(lore, loreNbt.asListView().stream().map(line -> GsonComponentSerializer.gson().deserialize(line.getValue())).toList());
        }

        {
            var lore = Stream.of("string test").map(Component::text).toList();
            item = item.withLore(lore);
            assertEquals(lore, item.getLore());
            var loreNbt = item.meta().toNBT().getCompound("display").<NBTString>getList("Lore");
            assertNotNull(loreNbt);
            assertEquals(1, loreNbt.getSize());
            assertEquals(lore, loreNbt.asListView().stream().map(line -> GsonComponentSerializer.gson().deserialize(line.getValue())).toList());
        }

        // Ensure that lore can be properly removed without residual (display compound)
        item = item.withLore(List.of());
        assertNull(item.meta().toNBT().get("display"));
    }
}
