package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.junit.jupiter.api.Test;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.*;

public class GraphConversionExecutorTest {
    @Test
    public void empty() {
        final Command foo = new Command("foo");
        var graph = Graph.fromCommand(foo);
        assertNull(graph.root().execution());
    }

    @Test
    public void defaultCondition() {
        final Command foo = new Command("foo");
        // Constant true
        {
            foo.setCondition((sender, commandString) -> true);
            var graph = Graph.fromCommand(foo);
            var execution = graph.root().execution();
            assertNotNull(execution);
            assertTrue(execution.test(null));
        }
        // Constant false
        {
            foo.setCondition((sender, commandString) -> false);
            var graph = Graph.fromCommand(foo);
            var execution = graph.root().execution();
            assertNotNull(execution);
            assertFalse(execution.test(null));
        }
    }

    @Test
    public void emptySyntaxCondition() {
        final Command foo = new Command("foo");
        foo.addSyntax(GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        assertNull(graph.root().next().get(0).execution());
    }

    @Test
    public void syntaxConditionTrue() {
        final Command foo = new Command("foo");
        foo.addConditionalSyntax((sender, context) -> true,
                GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var execution = graph.root().next().get(0).execution();
        assertNotNull(execution);
        assertTrue(execution.test(null));
    }

    @Test
    public void syntaxConditionFalse() {
        final Command foo = new Command("foo");
        foo.addConditionalSyntax((sender, context) -> false,
                GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var execution = graph.root().next().get(0).execution();
        assertNotNull(execution);
        assertFalse(execution.test(null));
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
