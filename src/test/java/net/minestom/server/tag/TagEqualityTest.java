package net.minestom.server.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test tags that can share cached values.
 */
public class TagEqualityTest {

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
    public void list() {
        var tag = Tag.String("test").list();
        assertTrue(tag.shareValue(tag));
    }

    //@Test
    public void similarList() {
        // TODO make work
        var tag = Tag.String("test").list();
        var tag2 = Tag.String("test").list();
        assertTrue(tag.shareValue(tag2));
    }
}
