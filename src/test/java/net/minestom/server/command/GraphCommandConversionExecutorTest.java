package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.junit.jupiter.api.Test;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.*;

public class GraphCommandConversionExecutorTest {
    @Test
    public void empty() {
        final Command foo = new Command("foo");
        var graph = Graph.fromCommand(foo);
        assertNull(graph.root().executor());
    }

    @Test
    public void defaultCondition() {
        final Command foo = new Command("foo");
        // Constant true
        {
            foo.setCondition((sender, commandString) -> true);
            var graph = Graph.fromCommand(foo);
            var executor = graph.root().executor();
            assertNotNull(executor);
            assertTrue(executor.test(null));
        }
        // Constant false
        {
            foo.setCondition((sender, commandString) -> false);
            var graph = Graph.fromCommand(foo);
            var executor = graph.root().executor();
            assertNotNull(executor);
            assertFalse(executor.test(null));
        }
    }

    @Test
    public void emptySyntaxCondition() {
        final Command foo = new Command("foo");
        foo.addSyntax(GraphCommandConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        assertNull(graph.root().next().get(0).executor());
    }

    @Test
    public void syntaxConditionTrue() {
        final Command foo = new Command("foo");
        foo.addConditionalSyntax((sender, context) -> true,
                GraphCommandConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var executor = graph.root().next().get(0).executor();
        assertNotNull(executor);
        assertTrue(executor.test(null));
    }

    @Test
    public void syntaxConditionFalse() {
        final Command foo = new Command("foo");
        foo.addConditionalSyntax((sender, context) -> false,
                GraphCommandConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var executor = graph.root().next().get(0).executor();
        assertNotNull(executor);
        assertFalse(executor.test(null));
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
