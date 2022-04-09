package net.minestom.server.tag;

import org.jglrxavpok.hephaistos.nbt.NBT;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ensure that NBT tag can be read from other tags properly.
 */
public class TagNbtTest {

    @Test
    public void list() {
        assertThrows(IllegalArgumentException.class, () -> Tag.NBT("nbt").list());
    }

    @Test
    public void map() {
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
}
