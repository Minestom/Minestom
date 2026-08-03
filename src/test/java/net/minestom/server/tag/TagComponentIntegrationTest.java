package net.minestom.server.tag;

import net.kyori.adventure.text.Component;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class TagComponentIntegrationTest {

    @Test
    public void get() {
        var component = Component.text("Hey");
        var tag = Tag.Component("component");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, component);
        assertEquals(component, handler.getTag(tag));
    }

    @Test
    public void empty() {
        var tag = Tag.Component("component");
        var handler = TagHandler.newHandler();
        assertNull(handler.getTag(tag));
    }

    @Test
    public void invalidTag(Env env) {
        var tag = Tag.Component("entry");
        var handler = TagHandler.newHandler();
        handler.setTag(Tag.Integer("entry"), 1);
        assertNull(handler.getTag(tag));
    }

    @Test
    public void nbtFallback(Env env) {
        var component = Component.text("Hey");
        var tag = Tag.Component("component");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, component);
        handler = TagHandler.fromCompound(handler.asCompound());
        assertEquals(component, handler.getTag(tag));
    }
}
