package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.permission.Permission;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.*;

public class GraphConversionExecutorTest {
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
        foo.addSyntax(GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        assertNull(graph.root().next().get(0).executor());
    }

    @Test
    public void syntaxConditionTrue() {
        final Command foo = new Command("foo");
        foo.addConditionalSyntax((sender, context) -> true,
                GraphConversionExecutorTest::dummyExecutor, Literal("first"));

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
                GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var executor = graph.root().next().get(0).executor();
        assertNotNull(executor);
        assertFalse(executor.test(null));
    }

    @Test
    public void syntaxCombinedCondition() {
        final Command foo = new Command("foo");
        foo.setCondition((sender, commandString) -> ((Sender) sender).id.startsWith("1"));
        foo.addConditionalSyntax((sender, context) -> true,
                GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        var graph = Graph.fromCommand(foo);
        assertEquals(1, graph.root().next().size());
        var executor = graph.root().next().get(0).executor();
        assertNotNull(executor);
        assertTrue(executor.test(new Sender("1")));
        assertFalse(executor.test(new Sender("2")));
    }

    @Test
    public void subCommandCombinedCondition() {
        final Command main = new Command("foo");
        main.setCondition((sender, commandString) -> ((Sender) sender).id.startsWith("1") && !((Sender) sender).id.endsWith("3"));
        final Command sub = new Command("sub");
        sub.setCondition((sender, commandString) -> ((Sender) sender).id.startsWith("12"));
        main.addSubcommand(sub);

        var graph = Graph.fromCommand(main);
        assertEquals(1, graph.root().next().size());
        var executor = graph.root().next().get(0).executor();
        assertNotNull(executor);
        assertTrue(executor.test(new Sender("12")));
        assertFalse(executor.test(new Sender("123")));
        assertFalse(executor.test(new Sender("1")));
    }

    @Test
    public void subCommandCombinedConditionDouble() {
        final Command main = new Command("foo");
        main.setCondition((sender, commandString) -> ((Sender) sender).id.startsWith("1") && !((Sender) sender).id.endsWith("4"));
        final Command sub = new Command("sub");
        sub.setCondition((sender, commandString) -> ((Sender) sender).id.startsWith("12"));
        final Command subSub = new Command("subSub");
        subSub.setCondition((sender, commandString) -> ((Sender) sender).id.startsWith("123"));

        main.addSubcommand(sub);
        sub.addSubcommand(subSub);

        var graph = Graph.fromCommand(main);
        var executor = graph.root().next().get(0).next().get(0).executor();
        assertNotNull(executor);
        assertTrue(executor.test(new Sender("123")));
        assertFalse(executor.test(new Sender("1234")));
        assertFalse(executor.test(new Sender("12")));
        assertFalse(executor.test(new Sender("1")));
    }

    @Test
    public void subCommandSyntaxCombinedConditionDouble() {
        final Command main = new Command("foo");
        main.setCondition((sender, commandString) -> ((Sender) sender).id.startsWith("1") && !((Sender) sender).id.endsWith("5"));
        final Command sub = new Command("sub");
        sub.setCondition((sender, commandString) -> ((Sender) sender).id.startsWith("12"));
        final Command subSub = new Command("subSub");
        subSub.setCondition((sender, commandString) -> ((Sender) sender).id.startsWith("123"));
        subSub.addConditionalSyntax((sender, context) -> ((Sender) sender).id.startsWith("1234"),
                GraphConversionExecutorTest::dummyExecutor, Literal("first"));

        main.addSubcommand(sub);
        sub.addSubcommand(subSub);

        var graph = Graph.fromCommand(main);
        var executor = graph.root().next().get(0).next().get(0).next().get(0).executor();
        assertNotNull(executor);
        assertTrue(executor.test(new Sender("1234")));
        assertFalse(executor.test(new Sender("12345")));
        assertFalse(executor.test(new Sender("123")));
        assertFalse(executor.test(new Sender("12")));
        assertFalse(executor.test(new Sender("1")));
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }

    record Sender(String id) implements CommandSender {
        @Override
        public @NotNull Set<Permission> getAllPermissions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull TagHandler tagHandler() {
            throw new UnsupportedOperationException();
        }
    }
}
