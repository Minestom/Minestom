package net.minestom.server.tag;

import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ensure that NBT tag can be read from other tags properly.
 */
public class TagNbtTest {

    @Test
    public void invalidListTag() {
        assertThrows(IllegalArgumentException.class, () -> Tag.NBT("nbt").list());
    }

    @Test
    public void invalidMapTag() {
        assertThrows(IllegalArgumentException.class, () -> Tag.NBT("nbt").map(nbt -> 1, NBT::Int));
    }

    @Test
    public void compoundRead() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("path1");

        var nbt = NBT.Compound(Map.of("key", NBT.Int(5)));
        handler.setTag(nbtTag, nbt);
        assertEquals(nbt, handler.getTag(nbtTag));

        var path = Tag.Integer("key").path("path1");
        assertEquals(5, handler.getTag(path));
    }

    @Test
    public void doubleCompoundRead() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("path1");

        var nbt = NBT.Compound(Map.of("path2", NBT.Compound(Map.of("key", NBT.Int(5)))));
        handler.setTag(nbtTag, nbt);
        assertEquals(nbt, handler.getTag(nbtTag));

        var path = Tag.Integer("key").path("path1", "path2");
        assertEquals(5, handler.getTag(path));
    }

    @Test
    public void compoundWrite() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("path1");

        var nbt = NBT.Compound(Map.of("key", NBT.Int(5)));
        handler.setTag(nbtTag, nbt);
        assertEquals(nbt, handler.getTag(nbtTag));

        var path = Tag.Integer("key").path("path1");
        handler.setTag(path, 10);
        assertEquals(10, handler.getTag(path));
        assertEquals(NBT.Compound(Map.of("key", NBT.Int(10))), handler.getTag(nbtTag));
    }

    @Test
    public void rawList() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("list");
        var list = NBT.List(NBTType.TAG_Int, NBT.Int(1));
        handler.setTag(nbtTag, list);
        assertEquals(list, handler.getTag(nbtTag));
    }

    @Test
    public void listConversion() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("list");
        var listTag = Tag.Integer("list").list();
        var list = NBT.List(NBTType.TAG_Int, NBT.Int(1));
        handler.setTag(nbtTag, list);

        assertEquals(list, handler.getTag(nbtTag));
        assertNotSame(list, handler.getTag(nbtTag));
        assertEquals(List.of(1), handler.getTag(listTag));
    }

    @Test
    public void rawArray() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("array");
        var array = NBT.IntArray(1, 2, 3);
        handler.setTag(nbtTag, array);
        assertEquals(array, handler.getTag(nbtTag));
    }
}
