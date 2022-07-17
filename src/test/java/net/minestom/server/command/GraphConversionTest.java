package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.junit.jupiter.api.Test;

import static net.minestom.server.command.builder.arguments.ArgumentType.Enum;
import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphConversionTest {
    @Test
    public void empty() {
        final Command foo = new Command("foo");
        var graph = Graph.builder(Literal("foo")).build();
        assertEqualsGraph(graph, foo);
    }

    @Test
    public void singleLiteral() {
        final Command foo = new Command("foo");
        var first = Literal("first");
        foo.addSyntax(GraphConversionTest::dummyExecutor, first);
        var graph = Graph.builder(Literal("foo"))
                .append(first).build();
        assertEqualsGraph(graph, foo);
    }

    @Test
    public void literalsPath() {
        final Command foo = new Command("foo");
        var first = Literal("first");
        var second = Literal("second");

        foo.addSyntax(GraphConversionTest::dummyExecutor, first);
        foo.addSyntax(GraphConversionTest::dummyExecutor, second);

        var graph = Graph.builder(Literal("foo"))
                .append(first).append(second)
                .build();
        assertEqualsGraph(graph, foo);
    }

    @Test
    public void doubleSyntax() {
        enum A {A, B, C, D, E}
        final Command foo = new Command("foo");

        var bar = Literal("bar");

        var baz = Literal("baz");
        var a = Enum("a", A.class);

        foo.addSyntax(GraphConversionTest::dummyExecutor, bar);
        foo.addSyntax(GraphConversionTest::dummyExecutor, baz, a);

        var graph = Graph.builder(Literal("foo"))
                .append(bar)
                .append(baz, builder ->
                        builder.append(a))
                .build();
        assertEqualsGraph(graph, foo);
    }

    @Test
    public void doubleSyntaxMerge() {
        final Command foo = new Command("foo");

        var bar = Literal("bar");
        var number = Integer("number");

        foo.addSyntax(GraphConversionTest::dummyExecutor, bar);
        foo.addSyntax(GraphConversionTest::dummyExecutor, bar, number);

        // The two syntax shall start from the same node
        var graph = Graph.builder(Literal("foo"))
                .append(bar, builder -> builder.append(number))
                .build();
        assertEqualsGraph(graph, foo);
    }

    @Test
    public void subcommand() {
        final Command main = new Command("main");
        final Command sub = new Command("sub");

        var bar = Literal("bar");
        var number = Integer("number");

        sub.addSyntax(GraphConversionTest::dummyExecutor, bar);
        sub.addSyntax(GraphConversionTest::dummyExecutor, bar, number);

        main.addSubcommand(sub);

        // The two syntax shall start from the same node
        var graph = Graph.builder(Literal("main"))
                .append(Literal("sub"), builder ->
                        builder.append(bar, builder1 -> builder1.append(number)))
                .build();
        assertEqualsGraph(graph, main);
    }

    @Test
    public void alias() {
        final Command main = new Command("main", "alias");
        var graph = Graph.builder(Word("main").from("main", "alias")).build();
        assertEqualsGraph(graph, main);
    }

    @Test
    public void aliases() {
        final Command main = new Command("main", "first", "second");
        var graph = Graph.builder(Word("main").from("main", "first", "second")).build();
        assertEqualsGraph(graph, main);
    }

    private static void assertEqualsGraph(Graph expected, Command command) {
        final Graph actual = Graph.fromCommand(command);
        assertTrue(expected.compare(actual, Graph.Comparator.TREE), () -> {
            System.out.println("Expected: " + expected);
            System.out.println("Actual:   " + actual);
            return "";
        });
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
