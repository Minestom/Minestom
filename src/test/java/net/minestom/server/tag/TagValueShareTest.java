package net.minestom.server.tag;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test tags that can share cached values.
 */
public class TagValueShareTest {

    record Entry(int value) {
    }

    @Test
    public void same() {
        var tag = Tag.String("test");
        assertTrue(tag.shareValue(tag));
    }

    @Test
    public void similar() {
        var tag = Tag.String("test");
        var tag2 = Tag.String("test");
        assertTrue(tag.shareValue(tag2));
    }

    @Test
    public void differentDefault() {
        var tag = Tag.String("test").defaultValue("test2");
        var tag2 = Tag.String("test").defaultValue("test3");
        assertTrue(tag.shareValue(tag2));
    }

    @Test
    public void differentType() {
        var tag = Tag.String("test");
        var tag2 = Tag.Integer("test");
        assertFalse(tag.shareValue(tag2));
    }

    @Test
    public void mapSame() {
        // Force identical functions
        Function<Integer, Entry> t1 = Entry::new;
        Function<Entry, Integer> t2 = Entry::value;

        var tag = Tag.Integer("key");
        var map1 = tag.map(t1, t2);
        var map2 = tag.map(t1, t2);
        assertTrue(map1.shareValue(map2));
    }

    @Test
    public void mapChild() {
        var intTag = Tag.Integer("key");
        var tag = intTag.map(Entry::new, Entry::value);
        assertFalse(intTag.shareValue(tag));
    }

    @Test
    public void list() {
        var tag = Tag.String("test").list();
        assertTrue(tag.shareValue(tag));
    }

    @Test
    public void listScope() {
        var tag = Tag.String("test");
        assertFalse(tag.shareValue(tag.list()));
    }

    @Test
    public void similarList() {
        var tag = Tag.String("test").list();
        var tag2 = Tag.String("test").list();
        assertTrue(tag.shareValue(tag2));
        assertTrue(tag.list().shareValue(tag2.list()));
    }

    @Test
    public void differentList() {
        var tag = Tag.String("test").list();
        var tag2 = Tag.String("test").list();
        assertFalse(tag.shareValue(tag2.list()));
        assertFalse(tag.list().shareValue(tag2.list().list()));
    }

    @Test
    public void differentListType() {
        var tag = Tag.String("test").list();
        var tag2 = Tag.Integer("test").list();
        assertFalse(tag.shareValue(tag2));
        assertFalse(tag.list().shareValue(tag2.list()));
    }

    @Test
    public void recordStructure() {
        var tag = Tag.Structure("test", Vec.class);
        var tag2 = Tag.Structure("test", Vec.class);
        assertTrue(tag.shareValue(tag2));
    }

    @Test
    public void recordStructureList() {
        var tag = Tag.Structure("test", Vec.class).list();
        var tag2 = Tag.Structure("test", Vec.class).list();
        assertTrue(tag.shareValue(tag2));
        assertTrue(tag.list().shareValue(tag2.list()));
    }
}
