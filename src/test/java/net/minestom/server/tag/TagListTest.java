package net.minestom.server.tag;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minestom.testing.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.*;

class TagListTest {

    @Test
    void basic() {
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
    void cache() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number").list();
        var val = List.of(1, 2, 3);

        handler.setTag(tag, val);
        assertSame(val, handler.getTag(tag));
    }

    @Test
    void recursiveCache() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number").list().list();
        var val = List.of(List.of(1, 2, 3), List.of(4, 5, 6));

        handler.setTag(tag, val);
        assertSame(val.get(0), handler.getTag(tag).get(0));
        assertSame(val.get(1), handler.getTag(tag).get(1));
        assertSame(val, handler.getTag(tag));
    }

    @Test
    void recursiveCacheIncorrect() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number").list().list();
        var val = List.of(List.of(1, 2, 3), new ArrayList<>(Arrays.asList(4, 5, 6)));

        handler.setTag(tag, val);
        assertSame(val.get(0), handler.getTag(tag).get(0));
        assertNotSame(val.get(1), handler.getTag(tag).get(1));
        assertNotSame(val, handler.getTag(tag));
        assertEquals(val, handler.getTag(tag));
    }

    @Test
    void snbt() {
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
    void empty() {
        var handler = TagHandler.newHandler();
        Tag<List<Integer>> tag = Tag.Integer("numbers").list();
        handler.setTag(tag, List.of());
        assertEquals(List.of(), handler.getTag(tag));
    }

    @Test
    void emptySnbt() {
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
    void removal() {
        var handler = TagHandler.newHandler();
        Tag<List<Integer>> tag = Tag.Integer("numbers").list();
        handler.setTag(tag, List.of(1));
        assertEquals(List.of(1), handler.getTag(tag));
        handler.removeTag(tag);
        assertNull(handler.getTag(tag));
    }

    @Test
    void removalSnbt() {
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
    void chaining() {
        var handler = TagHandler.newHandler();
        Tag<List<List<Integer>>> tag = Tag.Integer("numbers").list().list();
        var integers = List.of(List.of(1, 2, 3), List.of(4, 5, 6));
        handler.setTag(tag, integers);
        assertEquals(integers, handler.getTag(tag));
        handler.removeTag(tag);
        assertNull(handler.getTag(tag));
    }

    @Test
    void chainingSnbt() {
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

    @Test
    void defaultValue() {
        var handler = TagHandler.newHandler();
        var val = List.of(1, 2, 3);
        var tag = Tag.Integer("number").list().defaultValue(val);
        assertEquals(List.of(1, 2, 3), handler.getTag(tag));
    }

    @Test
    void defaultValueReset() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number").defaultValue(5);
        var list = tag.list();
        assertNull(handler.getTag(list));
        assertEquals(List.of(1, 2, 3), handler.getTag(list.defaultValue(List.of(1, 2, 3))));
    }

    @Test
    void immutability() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number").list();
        List<Integer> val = new ArrayList<>();
        val.add(1);

        handler.setTag(tag, val);
        assertNotSame(val, handler.getTag(tag));
        assertEquals(List.of(1), handler.getTag(tag));

        val.add(2); // Must not modify the nbt
        assertNotSame(val, handler.getTag(tag));
        assertEquals(List.of(1), handler.getTag(tag));
    }

    @Test
    void chainingImmutability() {
        var handler = TagHandler.newHandler();
        Tag<List<List<Integer>>> tag = Tag.Integer("numbers").list().list();
        List<List<Integer>> val = new ArrayList<>();
        val.add(new ArrayList<>(Arrays.asList(1, 2, 3)));
        val.add(new ArrayList<>(Arrays.asList(4, 5, 6)));

        handler.setTag(tag, val);
        assertNotSame(val, handler.getTag(tag));
        assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, 6)), handler.getTag(tag));

        // Must not modify the nbt
        val.get(0).add(7);
        val.get(1).add(8);
        val.add(new ArrayList<>(Arrays.asList(9, 10, 11)));
        assertNotSame(val, handler.getTag(tag));
        assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, 6)), handler.getTag(tag));
    }

    @Test
    void immutabilitySnbt() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("numbers").list();
        List<Integer> val = new ArrayList<>();
        val.add(1);

        handler.setTag(tag, val);
        assertEqualsSNBT("""
                {
                  "numbers": [1]
                }
                """, handler.asCompound());

        val.add(2); // Must not modify the nbt
        assertEqualsSNBT("""
                {
                  "numbers": [1]
                }
                """, handler.asCompound());
    }

    @Test
    void chainingImmutabilitySnbt() {
        var handler = TagHandler.newHandler();
        Tag<List<List<Integer>>> tag = Tag.Integer("numbers").list().list();
        List<List<Integer>> val = new ArrayList<>();
        val.add(new ArrayList<>(Arrays.asList(1, 2, 3)));
        val.add(new ArrayList<>(Arrays.asList(4, 5, 6)));

        handler.setTag(tag, val);
        assertEqualsSNBT("""
                {
                  "numbers":[
                    [1,2,3],
                    [4,5,6]
                  ]
                }
                """, handler.asCompound());


        // Must not modify the nbt
        val.get(0).add(7);
        val.get(1).add(8);
        val.add(new ArrayList<>(Arrays.asList(9, 10, 11)));
        assertEqualsSNBT("""
                {
                  "numbers":[
                    [1,2,3],
                    [4,5,6]
                  ]
                }
                """, handler.asCompound());
    }
}
