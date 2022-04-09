package net.minestom.server.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TagEqualityTest {

    @Test
    public void sameType() {
        var tag1 = Tag.Integer("key");
        var tag2 = Tag.Integer("key");
        assertEquals(tag1, tag1);
        assertEquals(tag2, tag2);
        assertEquals(tag1, tag2);
    }

    @Test
    public void differentKey() {
        var tag1 = Tag.Integer("key1");
        var tag2 = Tag.Integer("key2");
        assertNotEquals(tag1, tag2);
    }

    @Test
    public void sameList() {
        var tag1 = Tag.Integer("key").list();
        var tag2 = Tag.Integer("key").list();
        assertEquals(tag1, tag2);
    }

    @Test
    public void differentList() {
        var tag1 = Tag.Integer("key").list();
        var tag2 = Tag.Integer("key");
        assertNotEquals(tag1, tag2);
    }

    @Test
    public void unmatchedList() {
        var tag1 = Tag.Integer("key").list().list();
        var tag2 = Tag.Integer("key").list();
        assertNotEquals(tag1, tag2);
    }

    @Test
    public void samePath() {
        var tag1 = Tag.Integer("key").path("path");
        var tag2 = Tag.Integer("key").path("path");
        assertEquals(tag1, tag2);
    }

    @Test
    public void differentPath() {
        var tag1 = Tag.Integer("key").path("path");
        var tag2 = Tag.Integer("key").path("path2");
        assertNotEquals(tag1, tag2);
    }

    @Test
    public void unmatchedPath() {
        var tag1 = Tag.Integer("key").path("path", "path2");
        var tag2 = Tag.Integer("key").path("path");
        assertNotEquals(tag1, tag2);
    }
}
