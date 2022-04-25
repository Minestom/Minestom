package net.minestom.server.tag;

import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTInt;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.minestom.server.api.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Ensure that NBT tag can be read from other tags properly.
 */
public class TagNbtTest {

    @Test
    public void list() {
        var handler = TagHandler.newHandler();
        var tag = Tag.NBT("nbt").list();
        List<NBT> list = List.of(NBT.Int(1), NBT.Int(2), NBT.Int(3));
        handler.setTag(tag, list);
        assertEquals(list, handler.getTag(tag));
        assertEqualsSNBT("""
                {
                  "nbt": [1,2,3]
                }
                """, handler.asCompound());

        handler.removeTag(tag);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void map() {
        var handler = TagHandler.newHandler();
        var tag = Tag.NBT("nbt").map(nbt -> ((NBTInt) nbt).getValue(), NBT::Int);
        handler.setTag(tag, 5);
        assertEquals(5, handler.getTag(tag));
        assertEqualsSNBT("""
                {
                  "nbt":5
                }
                """, handler.asCompound());

        handler.removeTag(tag);
        assertEqualsSNBT("{}", handler.asCompound());
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
    public void compoundPathRead() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("compound").path("path");

        var nbt = NBT.Compound(Map.of("key", NBT.Int(5)));
        handler.setTag(nbtTag, nbt);
        assertEquals(nbt, handler.getTag(nbtTag));

        var path = Tag.Integer("key").path("path", "compound");
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
