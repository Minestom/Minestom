package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

    @Test
    public void testNames() {
        Command command = new Command("name1", "name2", "name3");

        assertEquals("name1", command.getName());
        assertArrayEquals(new String[]{"name2", "name3"}, command.getAliases());

        // command#getNames does not have any order guarantee, so that cannot be relied on
        assertEquals(Set.of("name1", "name2", "name3"), Set.of(command.getNames()));

        assertTrue(Command.isValidName(command, "name1"));
        assertTrue(Command.isValidName(command, "name2"));
    }

    @Test
    public void testGlobalListener() {
        var manager = new CommandManager();

        AtomicBoolean hasRun = new AtomicBoolean(false);

        var command = new Command("command") {
            @Override
            public void globalListener(@NotNull CommandSender sender, @NotNull CommandContext context, @NotNull String command) {
                hasRun.set(true);
                context.setArg("key", "value", "value");
            }
        };

        manager.register(command);

        AtomicBoolean checkSet = new AtomicBoolean(false);
        command.setDefaultExecutor((sender, context) -> checkSet.set("value".equals(context.get("key"))));

        manager.executeServerCommand("command");

        assertTrue(hasRun.get());
        assertTrue(checkSet.get());

    }
}
