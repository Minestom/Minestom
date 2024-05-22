package net.minestom.server.tag;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TagComponentTest {

    @Test
    void get() {
        var component = Component.text("Hey");
        var tag = Tag.Component("component");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, component);
        assertEquals(component, handler.getTag(tag));
    }

    @Test
    void empty() {
        var tag = Tag.Component("component");
        var handler = TagHandler.newHandler();
        assertNull(handler.getTag(tag));
    }

    @Test
    void invalidTag() {
        var tag = Tag.Component("entry");
        var handler = TagHandler.newHandler();
        handler.setTag(Tag.Integer("entry"), 1);
        assertNull(handler.getTag(tag));
    }

    @Test
    void nbtFallback() {
        var component = Component.text("Hey");
        var tag = Tag.Component("component");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, component);
        handler = TagHandler.fromCompound(handler.asCompound());
        assertEquals(component, handler.getTag(tag));
    }
}
