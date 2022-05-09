package net.minestom.server.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TagAtomicTest {

    @Test
    public void update() {
        var tag = Tag.Integer("coin");
        var handler = TagHandler.newHandler();
        handler.updateTag(tag, integer -> {
            assertNull(integer);
            return 5;
        });
        assertEquals(5, handler.getTag(tag));
        handler.updateTag(tag, integer -> {
            assertEquals(5, integer);
            return 10;
        });
        assertEquals(10, handler.getTag(tag));
    }

    @Test
    public void updateAndGet() {
        var tag = Tag.Integer("coin");
        var handler = TagHandler.newHandler();
        var result = handler.updateAndGetTag(tag, integer -> {
            assertNull(integer);
            return 5;
        });
        assertEquals(5, result);
        result = handler.updateAndGetTag(tag, integer -> {
            assertEquals(5, integer);
            return 10;
        });
        assertEquals(10, result);
    }

    @Test
    public void getAndUpdate() {
        var tag = Tag.Integer("coin");
        var handler = TagHandler.newHandler();
        var result = handler.getAndUpdateTag(tag, integer -> {
            assertNull(integer);
            return 5;
        });
        assertNull(result);
        result = handler.getAndUpdateTag(tag, integer -> {
            assertEquals(5, integer);
            return 10;
        });
        assertEquals(5, result);
    }
}
