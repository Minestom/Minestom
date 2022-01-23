package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.permission.Permission;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CommandParsingTest {

    @Test
    public void findCommand() {
        var dispatcher = new CommandDispatcher();
        assertNull(dispatcher.findCommand("name"));
        var command1 = new Command("name");
        dispatcher.register(command1);
        assertEquals(command1, dispatcher.findCommand("name"));
        dispatcher.unregister(command1);
        assertNull(dispatcher.findCommand("name"));
    }

    @Test
    public void parseDefault() {
        var dispatcher = new CommandDispatcher();
        var sender = new Sender();
        AtomicReference<String> data = new AtomicReference<>();
        var command = new Command("name");
        command.setDefaultExecutor((s, context) -> data.set("default"));
        dispatcher.register(command);

        dispatcher.execute(sender, "name");
        assertEquals("default", data.get());
    }

    @Test
    public void parseLiteral() {
        var dispatcher = new CommandDispatcher();
        var sender = new Sender();
        AtomicReference<String> data = new AtomicReference<>();
        var command = new Command("name");
        command.setDefaultExecutor((s, context) -> data.set("default"));
        command.addSyntax((s, context) -> data.set("literal"),
                ArgumentType.Literal("literal"));
        dispatcher.register(command);

        dispatcher.execute(sender, "name");
        assertEquals("default", data.get());

        data.set(null);
        dispatcher.execute(sender, "name literal");
        assertEquals("literal", data.get());

        // Do not ignore cases
        data.set(null);
        dispatcher.execute(sender, "name Literal");
        assertEquals("default", data.get());
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
