package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.junit.jupiter.api.Test;

import static net.minestom.server.command.builder.arguments.ArgumentType.Enum;
import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphCommandConversionTest {
    @Test
    public void empty() {
        final Command foo = new Command("foo");
        var graph = Graph.builder(Literal("foo")).build();
        assertEqualsGraph(graph, Graph.fromCommand(foo));
    }

    @Test
    public void singleLiteral() {
        final Command foo = new Command("foo");
        var first = Literal("first");
        foo.addSyntax(GraphCommandConversionTest::dummyExecutor, first);
        var graph = Graph.builder(Literal("foo"))
                .append(first).build();
        assertEqualsGraph(graph, Graph.fromCommand(foo));
    }

    @Test
    public void literalsPath() {
        final Command foo = new Command("foo");
        var first = Literal("first");
        var second = Literal("second");

        foo.addSyntax(GraphCommandConversionTest::dummyExecutor, first);
        foo.addSyntax(GraphCommandConversionTest::dummyExecutor, second);

        var graph = Graph.builder(Literal("foo"))
                .append(first).append(second)
                .build();
        assertEqualsGraph(graph, Graph.fromCommand(foo));
    }

    @Test
    public void doubleSyntax() {
        enum A {A, B, C, D, E}
        final Command foo = new Command("foo");

        var bar = Literal("bar");

        var baz = Literal("baz");
        var a = Enum("a", A.class);

        foo.addSyntax(GraphCommandConversionTest::dummyExecutor, bar);
        foo.addSyntax(GraphCommandConversionTest::dummyExecutor, baz, a);

        var graph = Graph.builder(Literal("foo"))
                .append(bar)
                .append(baz, builder ->
                        builder.append(a))
                .build();
        assertEqualsGraph(graph, Graph.fromCommand(foo));
    }

    @Test
    public void doubleSyntaxMerge() {
        final Command foo = new Command("foo");

        var bar = Literal("bar");
        var number = Integer("number");

        foo.addSyntax(GraphCommandConversionTest::dummyExecutor, bar);
        foo.addSyntax(GraphCommandConversionTest::dummyExecutor, bar, number);

        // The two syntax shall start from the same node
        var graph = Graph.builder(Literal("foo"))
                .append(bar, builder -> builder.append(number))
                .build();
        assertEqualsGraph(graph, Graph.fromCommand(foo));
    }

    private static void assertEqualsGraph(Graph expected, Graph actual) {
        assertTrue(expected.compare(actual, Graph.Comparator.TREE), () -> {
            System.out.println("Expected: " + expected);
            System.out.println("Actual:   " + actual);
            return "";
        });
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
