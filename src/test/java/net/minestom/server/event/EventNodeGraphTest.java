package net.minestom.server.event;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventNodeGraphTest {

    @Test
    public void single() {
        EventNode<Event> node = EventNode.all("main");
        verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0, List.of()));
    }

    @Test
    public void singleChild() {
        EventNode<Event> node = EventNode.all("main");
        node.addChild(EventNode.all("child"));
        verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                List.of(new EventNodeImpl.Graph("child", "Event", 0, List.of())
                )));
    }

    @Test
    public void childrenPriority() {
        {
            EventNode<Event> node = EventNode.all("main");
            node.addChild(EventNode.all("child1").setPriority(5));
            node.addChild(EventNode.all("child2").setPriority(10));
            verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                    List.of(new EventNodeImpl.Graph("child1", "Event", 5, List.of()),
                            new EventNodeImpl.Graph("child2", "Event", 10, List.of())
                    )));
        }
        {
            EventNode<Event> node = EventNode.all("main");
            node.addChild(EventNode.all("child2").setPriority(10));
            node.addChild(EventNode.all("child1").setPriority(5));
            verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                    List.of(new EventNodeImpl.Graph("child1", "Event", 5, List.of()),
                            new EventNodeImpl.Graph("child2", "Event", 10, List.of())
                    )));
        }
    }

    void verifyGraph(EventNode<?> n, EventNodeImpl.Graph graph) {
        EventNodeImpl<?> node = (EventNodeImpl<?>) n;
        var nodeGraph = node.createGraph();
        assertEquals(graph, nodeGraph, "Graphs are not equals");
        assertEquals(EventNodeImpl.createStringGraph(graph), EventNodeImpl.createStringGraph(nodeGraph), "String graphs are not equals");
        assertEquals(n.toString(), EventNodeImpl.createStringGraph(nodeGraph), "The node does not use createStringGraph");
    }
}
