package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.permission.Permission;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Set;
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

    private static final class Sender implements CommandSender {
        @Override
        public @NotNull Set<Permission> getAllPermissions() {
            return null;
        }

        @Override
        public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
            return null;
        }

        @Override
        public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        }
    }
}
