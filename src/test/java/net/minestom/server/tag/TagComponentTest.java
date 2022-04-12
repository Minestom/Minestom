package net.minestom.server.tag;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class TagComponentTest {

    @Test
    public void get() {
        var component = Component.text("Hey");
        var tag = Tag.Component("component");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, component);
        assertSame(component, handler.getTag(tag));
    }

    @Test
    public void empty() {
        var tag = Tag.Component("component");
        var handler = TagHandler.newHandler();
        assertNull(handler.getTag(tag));
    }

    @Test
    public void invalidTag() {
        var tag = Tag.Component("entry");
        var handler = TagHandler.newHandler();
        handler.setTag(Tag.Integer("entry"), 1);
        assertNull(handler.getTag(tag));
    }
}
