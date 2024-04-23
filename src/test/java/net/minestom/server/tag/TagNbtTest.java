package net.minestom.server.tag;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.kyori.adventure.nbt.IntArrayBinaryTag.intArrayBinaryTag;
import static net.kyori.adventure.nbt.IntBinaryTag.intBinaryTag;
import static net.kyori.adventure.nbt.ListBinaryTag.listBinaryTag;
import static net.minestom.testing.TestUtils.assertEqualsSNBT;
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
        List<BinaryTag> list = List.of(intBinaryTag(1), intBinaryTag(2), intBinaryTag(3));
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
        var tag = Tag.NBT("nbt").map(nbt -> ((IntBinaryTag) nbt).value(), IntBinaryTag::intBinaryTag);
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
    public void fromCompoundModify() {
        var compound = CompoundBinaryTag.builder().putInt("key", 5).build();
        var handler = TagHandler.fromCompound(compound);
        assertEquals(compound, handler.asCompound());
        assertEqualsSNBT("""
                {"key":5}
                """, handler.asCompound());

        handler.setTag(Tag.Integer("key"), 10);
        assertEquals(10, handler.getTag(Tag.Integer("key")));
        assertEqualsSNBT("""
                {"key":10}
                """, handler.asCompound());
        handler.setTag(Tag.Integer("key"), 15);
        assertEqualsSNBT("""
                {"key":15}
                """, handler.asCompound());
    }

    @Test
    public void fromCompoundModifyPath() {
        var compound = CompoundBinaryTag.builder().put("path", CompoundBinaryTag.builder().putInt("key", 5).build()).build();
        var handler = TagHandler.fromCompound(compound);
        var tag = Tag.Integer("key").path("path");

        handler.setTag(tag, 10);
        assertEquals(10, handler.getTag(tag));
        assertEqualsSNBT("""
                {"path":{"key":10}}
                """, handler.asCompound());
        handler.setTag(tag, 15);
        assertEqualsSNBT("""
                {"path":{"key":15}}
                """, handler.asCompound());
    }

    @Test
    public void fromCompoundModifyDoublePath() {
        var compound = CompoundBinaryTag.builder().put("path", CompoundBinaryTag.builder()
                .put("path2", CompoundBinaryTag.builder().putInt("key", 5).build()).build()).build();
        var handler = TagHandler.fromCompound(compound);
        var tag = Tag.Integer("key").path("path", "path2");

        handler.setTag(tag, 10);
        assertEquals(10, handler.getTag(tag));
        assertEqualsSNBT("""
                {"path":{"path2":{"key":10}}}
                """, handler.asCompound());
        handler.setTag(tag, 15);
        assertEqualsSNBT("""
                {"path":{"path2":{"key":15}}}
                """, handler.asCompound());
    }

    @Test
    public void compoundOverride() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("path1");

        var nbt1 = CompoundBinaryTag.from(Map.of("key", intBinaryTag(5)));
        var nbt2 = CompoundBinaryTag.from(Map.of("other-key", intBinaryTag(5)));
        handler.setTag(nbtTag, nbt1);
        assertEquals(nbt1, handler.getTag(nbtTag));

        handler.setTag(nbtTag, nbt2);
        assertEquals(nbt2, handler.getTag(nbtTag));
    }

    @Test
    public void compoundRead() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("path1");

        var nbt = CompoundBinaryTag.from(Map.of("key", intBinaryTag(5)));
        handler.setTag(nbtTag, nbt);
        assertEquals(nbt, handler.getTag(nbtTag));

        var path = Tag.Integer("key").path("path1");
        assertEquals(5, handler.getTag(path));
    }

    @Test
    public void compoundPathRead() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("compound").path("path");

        var nbt = CompoundBinaryTag.from(Map.of("key", intBinaryTag(5)));
        handler.setTag(nbtTag, nbt);
        assertEquals(nbt, handler.getTag(nbtTag));

        var path = Tag.Integer("key").path("path", "compound");
        assertEquals(5, handler.getTag(path));
    }

    @Test
    public void doubleCompoundRead() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("path1");

        var nbt = CompoundBinaryTag.from(Map.of("path2", CompoundBinaryTag.from(Map.of("key", intBinaryTag(5)))));
        handler.setTag(nbtTag, nbt);
        assertEquals(nbt, handler.getTag(nbtTag));

        var path = Tag.Integer("key").path("path1", "path2");
        assertEquals(5, handler.getTag(path));
    }

    @Test
    public void compoundWrite() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("path1");

        var nbt = CompoundBinaryTag.from(Map.of("key", intBinaryTag(5)));
        handler.setTag(nbtTag, nbt);
        assertEquals(nbt, handler.getTag(nbtTag));

        var path = Tag.Integer("key").path("path1");
        handler.setTag(path, 10);
        assertEquals(10, handler.getTag(path));
        assertEquals(CompoundBinaryTag.from(Map.of("key", intBinaryTag(10))), handler.getTag(nbtTag));
    }

    @Test
    public void rawList() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("list");
        var list = listBinaryTag(BinaryTagTypes.INT, List.of(intBinaryTag(1)));
        handler.setTag(nbtTag, list);
        assertEquals(list, handler.getTag(nbtTag));
    }

    @Test
    public void listConversion() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("list");
        var listTag = Tag.Integer("list").list();
        var list = listBinaryTag(BinaryTagTypes.INT, List.of(intBinaryTag(1)));
        handler.setTag(nbtTag, list);

        assertEquals(list, handler.getTag(nbtTag));
        assertNotSame(list, handler.getTag(nbtTag));
        assertEquals(List.of(1), handler.getTag(listTag));
    }

    @Test
    public void rawArray() {
        var handler = TagHandler.newHandler();
        var nbtTag = Tag.NBT("array");
        var array = intArrayBinaryTag(1, 2, 3);
        handler.setTag(nbtTag, array);
        assertEquals(array, handler.getTag(nbtTag));
    }
}
