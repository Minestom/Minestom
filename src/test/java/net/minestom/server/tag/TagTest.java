package net.minestom.server.tag;

import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TagTest {

    @Test
    public void testTag() {
        var mutable = new MutableNBTCompound();
        mutable.setInt("key", 5);
        var tag = Tag.Integer("key");
        var handler = TagHandler.fromCompound(new MutableNBTCompound());
        handler.setTag(tag, 5);
        assertEquals(mutable.toCompound(), handler.getTag(Tag.NBT), "NBT is not the same");

        // Removal
        handler.setTag(tag, null);
        assertEquals(new NBTCompound(), handler.getTag(Tag.NBT), "Tag must be removed when set to null");
    }

    @Test
    public void testSnbt() {
        var mutable = new MutableNBTCompound();
        mutable.setInt("key", 5);

        var reader = TagReadable.fromCompound(mutable);
        final String snbt = reader.getTag(Tag.SNBT);
        assertEquals(snbt, mutable.toCompound().toSNBT(), "SNBT is not the same");
    }

    @Test
    public void testDefault() {
        var nullable = Tag.String("key");
        var notNull = nullable.defaultValue("Hey");
        assertNotSame(nullable, notNull);

        var handler = TagHandler.fromCompound(new MutableNBTCompound());
        assertFalse(handler.hasTag(nullable));
        assertTrue(handler.hasTag(notNull)); // default value is set
        assertFalse(handler.hasTag(nullable));

        assertNull(handler.getTag(nullable));
        assertEquals("Hey", handler.getTag(notNull));
    }
}
