package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.junit.jupiter.api.Test;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.*;

public class GraphConversionExecutorTest {
    @Test
    public void defaultCondition() {
        final Command foo = new Command("foo");
        // Constant true
        {
            foo.setCondition((_, _) -> true);
            var graph = Graph.fromCommand(foo);
            var execution = graph.root().execution();
            assertNotNull(execution);
            assertTrue(execution.test(null));
        }
        // Constant false
        {
            foo.setCondition((_, _) -> false);
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
        var execution = graph.root().next().getFirst().execution();
        assertNotNull(execution);
        assertNull(execution.condition());
        assertNotNull(execution.executor());
    }

    @Test
    public void syntaxConditionTrue() {
        final Command foo = new Command("foo");
        foo.addConditionalSyntax((_, _) -> true,
                GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var execution = graph.root().next().getFirst().execution();
        assertNotNull(execution);
        assertTrue(execution.test(null));
    }

    @Test
    public void syntaxConditionFalse() {
        final Command foo = new Command("foo");
        foo.addConditionalSyntax((_, _) -> false,
                GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var execution = graph.root().next().getFirst().execution();
        assertNotNull(execution);
        assertFalse(execution.test(null));
    }

    @Test
    public void commandConditionFalse() {
        final Command foo = new Command("foo");
        foo.setCondition((_, _) -> false);
        final Graph graph = Graph.fromCommand(foo);
        final Graph.Execution execution = graph.root().execution();
        assertNotNull(execution);
        final CommandCondition condition = execution.condition();
        assertNotNull(condition);
        assertFalse(condition.canUse(null, null));
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
