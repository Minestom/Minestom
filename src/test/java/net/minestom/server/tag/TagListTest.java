package net.minestom.server.tag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.api.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TagListTest {

    @Test
    public void basic() {
        var handler = TagHandler.newHandler();
        Tag<Integer> tag = Tag.Integer("number");
        Tag<List<Integer>> list = tag.list();

        handler.setTag(tag, 5);
        assertEquals(5, handler.getTag(tag));
        assertNull(handler.getTag(list));

        handler.setTag(list, List.of(1, 2, 3));
        assertEquals(List.of(1, 2, 3), handler.getTag(list));
        assertNull(handler.getTag(tag));
    }

    @Test
    public void snbt() {
        var handler = TagHandler.newHandler();
        Tag<List<Integer>> tag = Tag.Integer("numbers").list();

        handler.setTag(tag, List.of(1, 2, 3));
        assertEqualsSNBT("""
                {
                  "numbers": [1,2,3]
                }
                """, handler.asCompound());
    }

    @Test
    public void empty() {
        var handler = TagHandler.newHandler();
        Tag<List<Integer>> tag = Tag.Integer("numbers").list();
        handler.setTag(tag, List.of());
        assertEquals(List.of(), handler.getTag(tag));
    }

    @Test
    public void emptySnbt() {
        var handler = TagHandler.newHandler();
        Tag<List<Integer>> tag = Tag.Integer("numbers").list();
        handler.setTag(tag, List.of());
        assertEqualsSNBT("""
                {
                  "numbers":[]
                }
                """, handler.asCompound());
    }

    @Test
    public void removal() {
        var handler = TagHandler.newHandler();
        Tag<List<Integer>> tag = Tag.Integer("numbers").list();
        handler.setTag(tag, List.of(1));
        assertEquals(List.of(1), handler.getTag(tag));
        handler.removeTag(tag);
        assertNull(handler.getTag(tag));
    }

    @Test
    public void removalSnbt() {
        var handler = TagHandler.newHandler();
        Tag<List<Integer>> tag = Tag.Integer("numbers").list();
        handler.setTag(tag, List.of(1));
        assertEqualsSNBT("""
                {
                  "numbers": [1]
                }
                """, handler.asCompound());
        handler.removeTag(tag);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void chaining() {
        var handler = TagHandler.newHandler();
        Tag<List<List<Integer>>> tag = Tag.Integer("numbers").list().list();
        var integers = List.of(List.of(1, 2, 3), List.of(4, 5, 6));
        handler.setTag(tag, integers);
        assertEquals(integers, handler.getTag(tag));
        handler.removeTag(tag);
        assertNull(handler.getTag(tag));
    }

    @Test
    public void chainingSnbt() {
        var handler = TagHandler.newHandler();
        Tag<List<List<Integer>>> tag = Tag.Integer("numbers").list().list();
        var integers = List.of(List.of(1, 2, 3), List.of(4, 5, 6));
        handler.setTag(tag, integers);
        assertEqualsSNBT("""
                {
                  "numbers":[
                    [1,2,3],
                    [4,5,6]
                  ]
                }
                """, handler.asCompound());
        handler.removeTag(tag);
        assertEqualsSNBT("{}", handler.asCompound());
    }
}
