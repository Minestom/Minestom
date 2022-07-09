package net.minestom.server.command;

import org.junit.jupiter.api.Test;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphMergeTest {
    @Test
    public void empty() {
        var graph1 = Graph.builder(Literal("foo")).build();
        var graph2 = Graph.builder(Literal("bar")).build();
        var result = Graph.builder(Literal(""))
                .append(Literal("foo"))
                .append(Literal("bar"))
                .build();
        assertEqualsGraph(result, Graph.merge(graph1, graph2));
    }

    @Test
    public void literals() {
        var graph1 = Graph.builder(Literal("foo")).append(Literal("1")).build();
        var graph2 = Graph.builder(Literal("bar")).append(Literal("2")).build();
        var result = Graph.builder(Literal(""))
                .append(Literal("foo"), builder -> builder.append(Literal("1")))
                .append(Literal("bar"), builder -> builder.append(Literal("2")))
                .build();
        assertEqualsGraph(result, Graph.merge(graph1, graph2));
    }

    private static void assertEqualsGraph(Graph expected, Graph actual) {
        assertTrue(expected.compare(actual, Graph.Comparator.TREE), () -> {
            System.out.println("Expected: " + expected);
            System.out.println("Actual: " + actual);
            return "";
        });
    }
}
