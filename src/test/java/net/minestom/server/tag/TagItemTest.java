package net.minestom.server.tag;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;

import static net.minestom.testing.TestUtils.assertEqualsSNBT;
import static net.minestom.testing.TestUtils.waitUntilCleared;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TagItemTest {

    @Test
    void get() {
        var item = ItemStack.of(Material.DIAMOND);
        var tag = Tag.ItemStack("item");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, item);

        assertEquals(item, handler.getTag(tag));
    }

    @Test
    void getDifferentObject() {
        var item = ItemStack.of(Material.DIAMOND);
        var handler = TagHandler.newHandler();
        handler.setTag(Tag.ItemStack("item"), item);

        assertEquals(item, handler.getTag(Tag.ItemStack("item")));
    }

    @Test
    void remove() {
        var item = ItemStack.of(Material.DIAMOND);
        var tag = Tag.ItemStack("item");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, item);
        assertEquals(item, handler.getTag(tag));

        handler.setTag(tag, null);
        assertNull(handler.getTag(tag));
    }

    @Test
    void gc() {
        var item = ItemStack.of(Material.DIAMOND);
        var tag = Tag.ItemStack("item");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, item);
        assertEquals(item, handler.getTag(tag));
        handler.setTag(tag, null);

        var ref = new WeakReference<>(item);
        //noinspection UnusedAssignment
        item = null;
        waitUntilCleared(ref);
    }

    @Test
    void invalidation() {
        var item = ItemStack.of(Material.DIAMOND);
        var item2 = ItemStack.of(Material.DIAMOND, 2);
        var handler = TagHandler.newHandler();

        var tag = Tag.ItemStack("item");
        handler.setTag(tag, item);
        assertEquals(item, handler.getTag(tag));
        handler.setTag(tag, item2);
        assertEquals(item2, handler.getTag(tag));
    }

    @Test
    void differentTagInvalidation() {
        var item = ItemStack.of(Material.DIAMOND);
        var item2 = ItemStack.of(Material.DIAMOND, 2);
        var handler = TagHandler.newHandler();

        var itemTag = Tag.ItemStack("item");
        var nbtTag = Tag.NBT("item");
        // Write the item using the ItemStack tag
        {
            handler.setTag(itemTag, item);
            assertEquals(item, handler.getTag(itemTag));
            assertEquals(item.toItemNBT(), handler.getTag(nbtTag));
        }
        // Override it with an NBT tag
        {
            handler.setTag(nbtTag, item2.toItemNBT());
            assertEquals(item2, handler.getTag(itemTag));
            assertEquals(item2.toItemNBT(), handler.getTag(nbtTag));
        }
    }

    @Test
    void snbt() {
        var handler = TagHandler.newHandler();
        var tag = Tag.ItemStack("item");
        handler.setTag(tag, ItemStack.of(Material.DIAMOND));
        assertEqualsSNBT("""
                {
                  "item": {
                    "id":"minecraft:diamond",
                    "count":1
                  }
                }
                """, handler.asCompound());
        handler.removeTag(tag);
        assertEqualsSNBT("{}", handler.asCompound());
    }
}
