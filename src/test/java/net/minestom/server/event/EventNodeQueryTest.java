package net.minestom.server.event;

import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventNodeQueryTest {

    @Test
    public void find() {
        var node = EventNode.all("main");
        assertEquals(List.of(), node.findChildren("test"));

        var child1 = EventNode.all("test");
        var child2 = EventNode.all("test");
        var child3 = EventNode.all("test3");

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);

        assertEquals(List.of(child1, child2), node.findChildren("test"));
        assertEquals(List.of(child3), node.findChildren("test3"));

        node.removeChild(child1);
        assertEquals(List.of(child2), node.findChildren("test"));
        assertEquals(List.of(child3), node.findChildren("test3"));
    }

    @Test
    public void findType() {
        var node = EventNode.all("main");
        assertEquals(List.of(), node.findChildren("test", Event.class));

        var child1 = EventNode.type("test", EventFilter.PLAYER);
        var child2 = EventNode.type("test", EventFilter.ENTITY);
        var child3 = EventNode.type("test3", EventFilter.ENTITY);

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);

        assertEquals(List.of(child1, child2), node.findChildren("test", Event.class));
        assertEquals(List.of(child1, child2), node.findChildren("test", EntityEvent.class));
        assertEquals(List.of(child1), node.findChildren("test", PlayerEvent.class));
        assertEquals(List.of(child3), node.findChildren("test3", EntityEvent.class));

        node.removeChild(child1);
        assertEquals(List.of(child2), node.findChildren("test", Event.class));
        assertEquals(List.of(child2), node.findChildren("test", EntityEvent.class));
        assertEquals(List.of(), node.findChildren("test", PlayerEvent.class));
        assertEquals(List.of(child3), node.findChildren("test3", EntityEvent.class));
    }

    @Test
    public void replace() {
        var node = EventNode.all("main");

        var child1 = EventNode.all("test");
        var child2 = EventNode.all("test");
        var child3 = EventNode.all("test3");

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);

        var tmp1 = EventNode.all("tmp1");
        var tmp2 = EventNode.all("tmp2");

        node.replaceChildren("test", tmp1);
        assertEquals(List.of(child2), node.findChildren("test"));
        assertEquals(List.of(tmp1), node.findChildren("tmp1"));

        node.replaceChildren("test3", tmp2);
        assertEquals(List.of(child2), node.findChildren("test"));
        assertEquals(List.of(tmp1), node.findChildren("tmp1"));
        assertEquals(List.of(), node.findChildren("test3"));
        assertEquals(List.of(tmp2), node.findChildren("tmp2"));
    }
}
