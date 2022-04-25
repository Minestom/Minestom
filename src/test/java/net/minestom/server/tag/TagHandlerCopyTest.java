package net.minestom.server.tag;

import org.junit.jupiter.api.Test;

import static net.minestom.server.api.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TagHandlerCopyTest {

    @Test
    public void copy() {
        var handler = TagHandler.newHandler();
        handler.setTag(Tag.String("key"), "test");

        var copy = handler.copy();
        assertEquals(handler.getTag(Tag.String("key")), copy.getTag(Tag.String("key")));
    }

    @Test
    public void copyCachePath() {
        var tag = Tag.String("key").path("path");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, "test");
        assertEqualsSNBT("""
                {"path":{"key":"test"}}
                """, handler.asCompound());

        var copy = handler.copy();
        handler.setTag(tag, "test2");
        assertEqualsSNBT("""
                {"path":{"key":"test2"}}
                """, handler.asCompound());
        assertEqualsSNBT("""
                {"path":{"key":"test"}}
                """, copy.asCompound());

        copy.setTag(tag, "test3");
        assertEquals("test3", copy.getTag(tag));
        assertEqualsSNBT("""
                {"path":{"key":"test3"}}
                """, copy.asCompound());
    }

    @Test
    public void copyCache() {
        var tag = Tag.String("key");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, "test");
        assertEqualsSNBT("""
                {"key":"test"}
                """, handler.asCompound());

        var copy = handler.copy();
        handler.setTag(tag, "test2");
        assertEqualsSNBT("""
                {"key":"test2"}
                """, handler.asCompound());
        assertEqualsSNBT("""
                {"key":"test"}
                """, copy.asCompound());

        copy.setTag(tag, "test3");
        assertEquals("test3", copy.getTag(tag));
        assertEqualsSNBT("""
                {"key":"test2"}
                 """, handler.asCompound());
        assertEqualsSNBT("""
                {"key":"test3"}
                """, copy.asCompound());
    }

    @Test
    public void copyRehashing() {
        var handler = TagHandler.newHandler();
        TagHandler handlerCopy;
        for (int i = 0; i < 1000; i++) {
            handlerCopy = handler.copy();
            var tag = Tag.Integer("copyRehashing" + i);
            handler.setTag(tag, i);
            assertNull(handlerCopy.getTag(tag));
        }
    }
}
