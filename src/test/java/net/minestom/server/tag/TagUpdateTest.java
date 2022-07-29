package net.minestom.server.tag;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static net.minestom.server.api.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TagUpdateTest {

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
    public void updateDefault() {
        var tag = Tag.Integer("coin").defaultValue(25);
        var handler = TagHandler.newHandler();
        handler.updateTag(tag, integer -> {
            assertEquals(25, integer);
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
    public void updateRemoval() {
        var tag = Tag.Integer("coin");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, 5);
        handler.updateTag(tag, integer -> {
            assertEquals(5, integer);
            return null;
        });
        assertNull(handler.getTag(tag));
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void updateRemovalPath() {
        var tag = Tag.Integer("coin").path("path");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, 5);
        handler.updateTag(tag, integer -> {
            assertEquals(5, integer);
            return null;
        });
        assertNull(handler.getTag(tag));
        assertEqualsSNBT("{}", handler.asCompound());
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

    @Test
    public void updateInner() {
        var tag = Tag.Structure("vec", Vec.class);
        var tagX = Tag.Double("x").path("vec");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, new Vec(5, 10, 15));
        handler.updateTag(tagX, x -> {
            assertEquals(5, x);
            return 7d;
        });
        assertEquals(7d, handler.getTag(tagX));
        assertEquals(new Vec(7, 10, 15), handler.getTag(tag));
    }
}
