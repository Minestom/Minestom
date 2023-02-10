package net.minestom.server.tag;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TagTest {

    @Test
    public void intGet() {
        var mutable = new MutableNBTCompound().setInt("key", 5);
        var tag = Tag.Integer("key");
        var handler = TagHandler.fromCompound(new MutableNBTCompound());
        handler.setTag(tag, 5);
        assertEquals(5, handler.getTag(tag));
        assertEquals(mutable.toCompound(), handler.asCompound(), "NBT is not the same");

        // Removal
        handler.setTag(tag, null);
        assertEquals(new NBTCompound(), handler.asCompound(), "Tag must be removed when set to null");
    }

    @Test
    public void intNull() {
        var handler = TagHandler.fromCompound(new MutableNBTCompound().set("key", NBT.Int(5)));
        // Removal
        var tag = Tag.Integer("key");
        handler.setTag(tag, null);
        assertFalse(handler.hasTag(tag));
        assertEquals(NBTCompound.EMPTY, handler.asCompound(), "Tag must be removed when set to null");
    }

    @Test
    public void intRemove() {
        var handler = TagHandler.fromCompound(new MutableNBTCompound().set("key", NBT.Int(5)));
        // Removal
        var tag = Tag.Integer("key");
        handler.removeTag(tag);
        assertFalse(handler.hasTag(tag));
        assertEquals(NBTCompound.EMPTY, handler.asCompound(), "Tag must be removed when set to null");
    }

    @Test
    public void snbt() {
        var mutable = new MutableNBTCompound().setInt("key", 5);
        var reader = TagHandler.fromCompound(mutable);
        assertEquals(reader.asCompound().toSNBT(), mutable.toCompound().toSNBT(), "SNBT is not the same");
    }

    @Test
    public void fromNbt() {
        var mutable = new MutableNBTCompound().setInt("key", 5);
        var handler = TagHandler.fromCompound(mutable);
        assertEquals(5, handler.getTag(Tag.Integer("key")));
        assertEquals(mutable.toCompound(), handler.asCompound(), "NBT is not the same");
    }

    @Test
    public void fromNbtCache() {
        // Ensure that TagHandler#asCompound reuse the same compound used for construction
        var compound = NBT.Compound(Map.of("key", NBT.Int(5)));
        var handler = TagHandler.fromCompound(compound);
        assertSame(compound, handler.asCompound(), "NBT is not the same");
    }

    @Test
    public void defaultValue() {
        var nullable = Tag.String("key");
        var notNull = nullable.defaultValue("Hey");
        assertNotSame(nullable, notNull);

        var handler = TagHandler.newHandler();
        assertFalse(handler.hasTag(nullable));
        assertTrue(handler.hasTag(notNull)); // default value is set
        assertFalse(handler.hasTag(nullable));

        assertNull(handler.getTag(nullable));
        assertEquals("Hey", handler.getTag(notNull));
    }

    @Test
    public void invalidType() {
        var tag1 = Tag.Integer("key");
        var tag2 = Tag.String("key");

        var handler = TagHandler.newHandler();
        handler.setTag(tag1, 5);
        assertEquals(5, handler.getTag(tag1));

        assertNull(handler.getTag(tag2));
        assertEquals("hey", handler.getTag(tag2.defaultValue("hey")));
    }

    @Test
    public void item() {
        var item = ItemStack.of(Material.DIAMOND);
        var tag = Tag.ItemStack("item");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, item);
        assertEquals(item, handler.getTag(tag));
    }

    @Test
    public void tagResizing() {
        var tag1 = Tag.Integer("tag1");
        var tag2 = Tag.Integer("tag2");
        var handler = TagHandler.newHandler();

        handler.setTag(tag1, 5);
        handler.setTag(tag2, 1);

        assertEquals(5, handler.getTag(tag1));
        assertEquals(1, handler.getTag(tag2));
    }

    @Test
    public void nbtResizing() {
        var handler = TagHandler.fromCompound(NBT.Compound(Map.of(
                "tag1", NBT.Int(5),
                "tag2", NBT.Int(1))));

        assertEquals(5, handler.getTag(Tag.Integer("tag1")));
        assertEquals(1, handler.getTag(Tag.Integer("tag2")));
    }

    @Test
    public void rehashing() {
        var handler = TagHandler.newHandler();
        for (int i = 0; i < 1000; i++) {
            handler.setTag(Tag.Integer("rehashing" + i), i);
            for (int j = i; j > 0; j--) {
                assertEquals(j, handler.getTag(Tag.Integer("rehashing" + j)));
            }
        }
    }
}
