package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.junit.jupiter.api.Test;

import static net.minestom.server.command.builder.arguments.ArgumentType.Enum;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandGraphTest {
    @Test
    public void empty() {
        final Command foo = new Command("foo");
        var graph = Graph.builder(Literal("foo")).build();
        assertEquals(graph, Graph.fromCommand(foo));
    }

    @Test
    public void literal() {
        final Command foo = new Command("foo");
        var first = Literal("first");
        foo.addSyntax(CommandGraphTest::dummyExecutor, first);
        var graph = Graph.builder(Literal("foo"))
                .append(first)
                .build();
        assertEquals(graph, Graph.fromCommand(foo));
    }

    @Test
    public void literalsPath() {
        final Command foo = new Command("foo");
        var first = Literal("first");
        var second = Literal("second");

        foo.addSyntax(CommandGraphTest::dummyExecutor, first);
        foo.addSyntax(CommandGraphTest::dummyExecutor, second);

        var graph = Graph.builder(Literal("foo"))
                .append(first)
                .append(second)
                .build();
        assertEquals(graph, Graph.fromCommand(foo));
    }

    @Test
    public void doubleSyntax() {
        enum A {A, B, C, D, E}
        final Command foo = new Command("foo");

        var bar = Literal("bar");

        var baz = Literal("baz");
        var a = Enum("a", A.class);

        foo.addSyntax(CommandGraphTest::dummyExecutor, bar);
        foo.addSyntax(CommandGraphTest::dummyExecutor, baz, a);

        var graph = Graph.builder(Literal("foo"))
                .append(bar)
                .append(baz, builder ->
                        builder.append(a))
                .build();
        assertEquals(graph, Graph.fromCommand(foo));
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
