package net.minestom.server.command;

import net.kyori.adventure.identity.Identity;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.tag.TagHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

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
