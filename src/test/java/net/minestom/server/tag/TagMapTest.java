package net.minestom.server.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TagMapTest {

    private record Entry(int value) {
    }

    @Test
    void map() {
        var handler = TagHandler.newHandler();
        var intTag = Tag.Integer("key");
        var tag = intTag.map(Entry::new, Entry::value);

        handler.setTag(tag, new Entry(1));
        assertEquals(1, handler.getTag(intTag));
        assertEquals(new Entry(1), handler.getTag(tag));
    }

    @Test
    void mapDefault() {
        var handler = TagHandler.newHandler();
        var intTag = Tag.Integer("key");
        var tag = intTag.map(Entry::new, Entry::value);

        assertEquals(new Entry(1), handler.getTag(tag.defaultValue(new Entry(1))));

        handler.setTag(tag, new Entry(2));
        assertEquals(2, handler.getTag(intTag));
        assertEquals(new Entry(2), handler.getTag(tag));
    }

    @Test
    void mapDefaultAbsent() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("key").map(Entry::new, Entry::value);
        assertNull(handler.getTag(tag));
    }
}
