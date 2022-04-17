package net.minestom.server.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TagMapTest {

    private record Entry(int value) {
    }

    @Test
    public void map() {
        var handler = TagHandler.newHandler();
        var intTag = Tag.Integer("key");
        var tag = intTag.map(Entry::new, Entry::value);

        handler.setTag(tag, new Entry(1));
        assertEquals(1, handler.getTag(intTag));
        assertEquals(new Entry(1), handler.getTag(tag));
    }

    @Test
    public void mapDefault() {
        var handler = TagHandler.newHandler();
        var intTag = Tag.Integer("key");
        var tag = intTag.map(Entry::new, Entry::value);

        assertNull(handler.getTag(tag));
        assertEquals(new Entry(1), handler.getTag(tag.defaultValue(new Entry(1))));

        handler.setTag(tag, new Entry(2));
        assertEquals(2, handler.getTag(intTag));
        assertEquals(new Entry(2), handler.getTag(tag));
    }
}
