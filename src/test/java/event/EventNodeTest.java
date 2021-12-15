package event;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class EventNodeTest {

    class EventTest implements Event {
    }

    @Test
    public void testCall() {
        var node = EventNode.all("main");
        AtomicBoolean result = new AtomicBoolean(false);
        var listener = EventListener.of(EventTest.class, eventTest -> result.set(true));
        node.addListener(listener);
        assertFalse(result.get(), "The event should not be called before the call");
        node.call(new EventTest());
        assertTrue(result.get(), "The event should be called after the call");

        // Test removal
        result.set(false);
        node.removeListener(listener);
        node.call(new EventTest());
        assertFalse(result.get(), "The event should not be called after the removal");
    }

    @Test
    public void testChildren() {
        var node = EventNode.all("main");
        AtomicInteger result = new AtomicInteger(0);
        var child1 = EventNode.all("child1").setPriority(1)
                .addListener(EventTest.class, eventTest -> {
                    assertEquals(0, result.get(), "child1 should be called before child2");
                    result.set(1);
                });
        var child2 = EventNode.all("child2").setPriority(2)
                .addListener(EventTest.class, eventTest -> {
                    assertEquals(1, result.get(), "child2 should be called after child1");
                    result.set(2);
                });
        node.addChild(child1);
        node.addChild(child2);
        assertEquals(node.getChildren().size(), 2, "The node should have 2 children");
        node.call(new EventTest());
        assertEquals(2, result.get(), "The event should be called after the call");

        // Test removal
        result.set(0);
        node.removeChild(child2);
        assertEquals(node.getChildren().size(), 1, "The node should have 1 child");
        node.call(new EventTest());
        assertEquals(1, result.get(), "child2 should has been removed");

        result.set(0);
        node.removeChild(child1);
        node.call(new EventTest());
        assertTrue(node.getChildren().isEmpty(), "The node should have no child left");
        assertEquals(0, result.get(), "The event should not be called after the removal");
    }

}
