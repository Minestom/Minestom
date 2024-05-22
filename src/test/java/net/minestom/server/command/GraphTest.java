package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.*;

class GraphTest {
    @Test
    void empty() {
        var result = Graph.builder(Literal(""))
                .build();
        var node = result.root();
        assertEquals(Literal(""), node.argument());
        assertTrue(node.next().isEmpty());
    }

    @Test
    void next() {
        var result = Graph.builder(Literal(""))
                .append(Literal("foo"))
                .build();
        var node = result.root();
        assertEquals(Literal(""), node.argument());
        assertEquals(1, node.next().size());
        assertEquals(Literal("foo"), node.next().get(0).argument());
    }

    @Test
    void immutableNextBuilder() {
        var result = Graph.builder(Literal(""))
                .append(Literal("foo"))
                .append(Literal("bar"))
                .build();
        var node = result.root();
        assertThrows(Exception.class, () -> result.root().next().add(node));
        assertThrows(Exception.class, () -> result.root().next().get(0).next().add(node));
    }

    @Test
    void immutableNextCommand() {
        final Command foo = new Command("foo");
        var first = Literal("first");
        foo.addSyntax(GraphTest::dummyExecutor, first);
        var result = Graph.fromCommand(foo);

        var node = result.root();
        assertThrows(Exception.class, () -> result.root().next().add(node));
        assertThrows(Exception.class, () -> result.root().next().get(0).next().add(node));
    }

    @Test
    void immutableNextCommands() {
        final Command foo, bar;

        {
            var first = Literal("first");

            foo = new Command("foo");
            foo.addSyntax(GraphTest::dummyExecutor, first);

            bar = new Command("foo");
            bar.addSyntax(GraphTest::dummyExecutor, first);
        }

        var result = Graph.merge(List.of(foo, bar));

        var node = result.root();
        assertThrows(Exception.class, () -> result.root().next().add(node));
        assertThrows(Exception.class, () -> result.root().next().get(0).next().add(node));
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
