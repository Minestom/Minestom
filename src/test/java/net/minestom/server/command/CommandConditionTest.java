package net.minestom.server.command;

import net.kyori.adventure.identity.Identity;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.tag.TagHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.*;

public class CommandConditionTest {

    @Test
    public void mainCondition() {
        var dispatcher = new CommandDispatcher();
        assertNull(dispatcher.findCommand("name"));
        var sender = new Sender();
        var sender2 = new Sender();

        AtomicBoolean called = new AtomicBoolean(false);

        var command1 = new Command("name");
        command1.setDefaultExecutor((sender1, context) -> called.set(true));
        command1.setCondition((s, commandString) -> s == sender);

        dispatcher.register(command1);

        dispatcher.execute(sender, "name");
        assertTrue(called.get());

        called.set(false);
        dispatcher.execute(sender2, "name");
        assertFalse(called.get());
    }

    @Test
    public void subCondition() {
        var dispatcher = new CommandDispatcher();
        assertNull(dispatcher.findCommand("name"));
        var sender = new Sender();
        var sender2 = new Sender();

        AtomicBoolean called = new AtomicBoolean(false);

        var command1 = new Command("name");
        command1.setDefaultExecutor((sender1, context) -> called.set(true));

        {
            var sub = new Command("sub");
            sub.setDefaultExecutor((sender1, context) -> called.set(true));
            sub.setCondition((s, commandString) -> s == sender);

            command1.addSubcommand(sub);
        }

        dispatcher.register(command1);

        // Direct command
        {
            dispatcher.execute(sender, "name");
            assertTrue(called.get());

            called.set(false);
            dispatcher.execute(sender2, "name");
            assertTrue(called.get());
        }

        // Subcommand
        {
            called.set(false);
            dispatcher.execute(sender, "name sub");
            assertTrue(called.get());

            called.set(false);
            dispatcher.execute(sender2, "name sub");
            assertFalse(called.get());
        }
    }

    @Test
    public void subConditionOverride() {
        var dispatcher = new CommandDispatcher();
        assertNull(dispatcher.findCommand("name"));
        var sender = new Sender();
        var sender2 = new Sender();

        AtomicBoolean called = new AtomicBoolean(false);

        var command1 = new Command("name");
        command1.setDefaultExecutor((sender1, context) -> called.set(true));
        command1.setCondition((s, commandString) -> s == sender);

        {
            var sub = new Command("sub");
            sub.setDefaultExecutor((sender1, context) -> called.set(true));
            command1.addSubcommand(sub);
        }

        dispatcher.register(command1);

        // Direct command
        {
            dispatcher.execute(sender, "name");
            assertTrue(called.get());

            called.set(false);
            dispatcher.execute(sender2, "name");
            assertFalse(called.get());
        }

        // Subcommand
        {
            called.set(false);
            dispatcher.execute(sender, "name sub");
            assertTrue(called.get());

            called.set(false);
            dispatcher.execute(sender2, "name sub");
            assertFalse(called.get(), "Subcommand execution should have been cancelled by parent command condition");
        }
    }

    @Test
    public void conditionBypassedByZeroArgSyntax() {
        var dispatcher = new CommandDispatcher();
        var adminSender = new Sender();
        var normalSender = new Sender();

        AtomicBoolean called = new AtomicBoolean(false);

        var command = new Command("admin");
        command.setCondition((sender, commandString) -> sender == adminSender);
        command.addSyntax((sender, context) -> called.set(true));

        dispatcher.register(command);

        dispatcher.execute(adminSender, "admin");
        assertTrue(called.get(), "Admin should be able to execute");

        called.set(false);
        dispatcher.execute(normalSender, "admin");
        assertFalse(called.get(), "Normal user should be blocked by command condition, but bug allows execution!");
    }

    @Test
    public void bothCommandAndSyntaxConditionsChecked() {
        var dispatcher = new CommandDispatcher();
        var sender1 = new Sender();
        var sender2 = new Sender();
        var sender3 = new Sender();

        AtomicBoolean called = new AtomicBoolean(false);

        var command = new Command("test");
        command.setCondition((sender, commandString) -> sender == sender1 || sender == sender2);
        command.addConditionalSyntax((sender, commandString) -> sender == sender1 || sender == sender3,
                (sender, context) -> called.set(true));

        dispatcher.register(command);

        dispatcher.execute(sender1, "test");
        assertTrue(called.get(), "sender1 should satisfy both conditions");

        called.set(false);
        dispatcher.execute(sender2, "test");
        assertFalse(called.get(), "sender2 should be blocked by syntax condition");

        called.set(false);
        dispatcher.execute(sender3, "test");
        assertFalse(called.get(), "sender3 should be blocked by command condition");
    }

    @Test
    public void multipleZeroArgSyntaxesWithConditions() {
        var dispatcher = new CommandDispatcher();
        var sender1 = new Sender();
        var sender2 = new Sender();
        var sender3 = new Sender();

        AtomicInteger executionCount = new AtomicInteger(0);

        var command = new Command("multi");
        command.setCondition((sender, commandString) -> sender == sender1 || sender == sender2);

        // First zero-arg syntax with additional condition
        command.addConditionalSyntax((sender, commandString) -> sender == sender1,
                (sender, context) -> executionCount.incrementAndGet());

        dispatcher.register(command);

        // sender1 should work (passes both conditions)
        dispatcher.execute(sender1, "multi");
        assertEquals(1, executionCount.get(), "sender1 should execute successfully");

        // sender2 should be blocked by syntax condition
        executionCount.set(0);
        dispatcher.execute(sender2, "multi");
        assertEquals(0, executionCount.get(), "sender2 should be blocked by syntax condition");

        // sender3 should be blocked by command condition
        dispatcher.execute(sender3, "multi");
        assertEquals(0, executionCount.get(), "sender3 should be blocked by command condition");
    }

    @Test
    public void defaultExecutorWithConditionAndSyntaxes() {
        var dispatcher = new CommandDispatcher();
        var adminSender = new Sender();
        var normalSender = new Sender();

        AtomicBoolean defaultCalled = new AtomicBoolean(false);
        AtomicBoolean syntaxCalled = new AtomicBoolean(false);

        var command = new Command("cmd");
        command.setCondition((sender, commandString) -> sender == adminSender);
        command.setDefaultExecutor((sender, context) -> defaultCalled.set(true));
        command.addSyntax((sender, context) -> syntaxCalled.set(true), Integer("value"));

        dispatcher.register(command);

        // Admin executing without args should trigger default executor
        dispatcher.execute(adminSender, "cmd");
        assertTrue(defaultCalled.get(), "Admin should execute default executor");
        assertFalse(syntaxCalled.get());

        // Normal user should be blocked even from default executor
        defaultCalled.set(false);
        dispatcher.execute(normalSender, "cmd");
        assertFalse(defaultCalled.get(), "Normal user should be blocked from default executor");
        assertFalse(syntaxCalled.get());

        // Admin with valid syntax
        dispatcher.execute(adminSender, "cmd 42");
        assertTrue(syntaxCalled.get(), "Admin should execute syntax");

        // Normal user should be blocked from syntax too
        syntaxCalled.set(false);
        dispatcher.execute(normalSender, "cmd 42");
        assertFalse(syntaxCalled.get(), "Normal user should be blocked from syntax");
    }

    @Test
    public void deeplyNestedSubcommandConditions() {
        var dispatcher = new CommandDispatcher();
        var adminSender = new Sender();
        var normalSender = new Sender();

        AtomicBoolean called = new AtomicBoolean(false);

        var rootCmd = new Command("root");
        rootCmd.setCondition((sender, commandString) -> sender == adminSender);

        var level1 = new Command("level1");
        var level2 = new Command("level2");
        var level3 = new Command("level3");
        level3.setDefaultExecutor((sender, context) -> called.set(true));

        level2.addSubcommand(level3);
        level1.addSubcommand(level2);
        rootCmd.addSubcommand(level1);

        dispatcher.register(rootCmd);

        // Admin should be able to execute deeply nested command
        dispatcher.execute(adminSender, "root level1 level2 level3");
        assertTrue(called.get(), "Admin should execute deeply nested command");

        // Normal user should be blocked at root level
        called.set(false);
        dispatcher.execute(normalSender, "root level1 level2 level3");
        assertFalse(called.get(), "Normal user should be blocked by root condition");
    }

    @Test
    public void syntaxConditionOnlyNoCommandCondition() {
        var dispatcher = new CommandDispatcher();
        var sender1 = new Sender();
        var sender2 = new Sender();

        AtomicBoolean called = new AtomicBoolean(false);

        var command = new Command("test");
        // No command condition set
        command.addConditionalSyntax((sender, commandString) -> sender == sender1,
                (sender, context) -> called.set(true));

        dispatcher.register(command);

        // sender1 should execute (passes syntax condition)
        dispatcher.execute(sender1, "test");
        assertTrue(called.get(), "sender1 should execute with syntax condition");

        // sender2 should be blocked (fails syntax condition)
        called.set(false);
        dispatcher.execute(sender2, "test");
        assertFalse(called.get(), "sender2 should be blocked by syntax condition");
    }

    @Test
    public void mixedSyntaxConditions() {
        var dispatcher = new CommandDispatcher();
        var sender1 = new Sender();
        var sender2 = new Sender();
        var sender3 = new Sender();

        AtomicInteger whichSyntax = new AtomicInteger(0);

        var command = new Command("mixed");
        command.setCondition((sender, commandString) -> sender == sender1 || sender == sender2);

        // Syntax 1: zero-arg, no additional condition (should use command condition only)
        command.addSyntax((sender, context) -> whichSyntax.set(1));

        // Syntax 2: with arg and additional condition
        command.addConditionalSyntax((sender, commandString) -> sender == sender1,
                (sender, context) -> whichSyntax.set(2), Literal("special"));

        dispatcher.register(command);

        // sender1 can execute both syntaxes
        dispatcher.execute(sender1, "mixed");
        assertEquals(1, whichSyntax.get(), "sender1 should execute syntax 1");

        whichSyntax.set(0);
        dispatcher.execute(sender1, "mixed special");
        assertEquals(2, whichSyntax.get(), "sender1 should execute syntax 2");

        // sender2 can execute syntax 1 but not syntax 2
        whichSyntax.set(0);
        dispatcher.execute(sender2, "mixed");
        assertEquals(1, whichSyntax.get(), "sender2 should execute syntax 1");

        whichSyntax.set(0);
        dispatcher.execute(sender2, "mixed special");
        assertEquals(0, whichSyntax.get(), "sender2 should be blocked from syntax 2");

        // sender3 should be blocked from everything
        dispatcher.execute(sender3, "mixed");
        assertEquals(0, whichSyntax.get(), "sender3 should be blocked by command condition");

        dispatcher.execute(sender3, "mixed special");
        assertEquals(0, whichSyntax.get(), "sender3 should be blocked by command condition");
    }

    @Test
    public void subcommandWithOwnConditionRequiresBoth() {
        var dispatcher = new CommandDispatcher();
        var sender1 = new Sender();
        var sender2 = new Sender();
        var sender3 = new Sender();

        AtomicBoolean called = new AtomicBoolean(false);

        var parent = new Command("parent");
        parent.setCondition((sender, commandString) -> sender == sender1 || sender == sender2);

        var child = new Command("child");
        child.setCondition((sender, commandString) -> sender == sender1 || sender == sender3);
        child.setDefaultExecutor((sender, context) -> called.set(true));

        parent.addSubcommand(child);
        dispatcher.register(parent);

        // sender1 passes both conditions
        dispatcher.execute(sender1, "parent child");
        assertTrue(called.get(), "sender1 should pass both conditions");

        // sender2 passes parent but not child
        called.set(false);
        dispatcher.execute(sender2, "parent child");
        assertFalse(called.get(), "sender2 should be blocked by child condition");

        // sender3 passes child but not parent
        dispatcher.execute(sender3, "parent child");
        assertFalse(called.get(), "sender3 should be blocked by parent condition");
    }

    @Test
    public void zeroArgSyntaxAndDefaultExecutorWithCondition() {
        var dispatcher = new CommandDispatcher();
        var adminSender = new Sender();
        var normalSender = new Sender();

        AtomicBoolean defaultCalled = new AtomicBoolean(false);
        AtomicBoolean syntaxCalled = new AtomicBoolean(false);

        var command = new Command("cmd");
        command.setCondition((sender, commandString) -> sender == adminSender);
        command.setDefaultExecutor((sender, context) -> defaultCalled.set(true));
        command.addSyntax((sender, context) -> syntaxCalled.set(true)); // zero-arg syntax

        dispatcher.register(command);

        // Admin should execute the syntax (syntax takes precedence over default executor)
        dispatcher.execute(adminSender, "cmd");
        assertTrue(syntaxCalled.get(), "Admin should execute zero-arg syntax");
        assertFalse(defaultCalled.get(), "Default executor should not be called when syntax matches");

        // Normal user should be blocked
        syntaxCalled.set(false);
        dispatcher.execute(normalSender, "cmd");
        assertFalse(syntaxCalled.get(), "Normal user should be blocked from syntax");
        assertFalse(defaultCalled.get(), "Normal user should be blocked from default executor");
    }

    @Test
    public void testGraphZeroArgCondition() {
        var adminSender = new Sender();
        var normalSender = new Sender();

        var command = new Command("admin");
        command.setCondition((sender, commandString) -> sender == adminSender);
        command.addSyntax((sender, context) -> {});

        Graph graph = Graph.fromCommand(command);
        Graph.Node root = graph.root();

        assertNotNull(root.execution(), "Root node should have execution");
        assertNotNull(root.execution().condition(), "Root node should preserve command condition");
        assertFalse(root.execution().test(normalSender), "Normal sender should fail condition check");
        assertTrue(root.execution().test(adminSender), "Admin sender should pass condition check");
    }

    @Test
    public void graphPreservesConditionsForSyntaxWithArguments() {
        var adminSender = new Sender();

        var command = new Command("admin");
        command.setCondition((sender, commandString) -> sender == adminSender);
        command.addSyntax((sender, context) -> {}, Integer("value"));

        Graph graph = Graph.fromCommand(command);
        Graph.Node root = graph.root();

        assertNotNull(root.execution(), "Root should have execution");
        assertNotNull(root.execution().condition(), "Root should have command condition");

        // Check that the argument node also has condition info propagated
        assertEquals(1, root.next().size(), "Should have one child for the Integer argument");
        Graph.Node argNode = root.next().getFirst();
        assertNotNull(argNode.execution(), "Argument node should have execution");
    }

    @Test
    public void graphComparisonDetectsConditionDifferences() {
        var adminSender = new Sender();

        var command1 = new Command("test");
        command1.setCondition((sender, commandString) -> sender == adminSender);
        command1.addSyntax((sender, context) -> {});

        var command2 = new Command("test");
        // No condition
        command2.addSyntax((sender, context) -> {});

        Graph graph1 = Graph.fromCommand(command1);
        Graph graph2 = Graph.fromCommand(command2);

        assertFalse(graph1.compare(graph2, Graph.Comparator.TREE),
                "Graphs with different conditions should not be equal");
    }

    private static final class Sender implements CommandSender {
        @Override
        public TagHandler tagHandler() {
            return null;
        }

        @Override
        public Identity identity() {
            return Identity.nil();
        }
    }
}
