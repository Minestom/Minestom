package net.minestom.server.command;

import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphMergeTest {
    @Test
    public void empty() {
        var graph1 = Graph.builder(Literal("foo")).build();
        var graph2 = Graph.builder(Literal("bar")).build();

        var result = Graph.builder(Literal(""))
                .append(Literal("foo"))
                .append(Literal("bar"))
                .build();
        assertEquals(result, Graph.merge(List.of(graph1, graph2)));
    }

    @Test
    public void literals() {
        var graph1 = Graph.builder(Literal("foo")).append(Literal("1")).build();
        var graph2 = Graph.builder(Literal("bar")).append(Literal("2")).build();

        var result = Graph.builder(Literal(""))
                .append(Literal("foo"), builder -> builder.append(Literal("1")))
                .append(Literal("bar"), builder -> builder.append(Literal("2")))
                .build();
        assertEquals(result, Graph.merge(List.of(graph1, graph2)));
    }
}
