package net.minestom.server.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TagHandlerCopyTest {

    @Test
    public void copy() {
        var handler1 = TagHandler.newHandler();
        handler1.setTag(Tag.String("key"), "test");

        var handler2 = handler1.copy();
        assertEquals(handler1.getTag(Tag.String("key")), handler2.getTag(Tag.String("key")));
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
