package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CommandExecutionTest {

    @Test
    public void testAndCommand() {
        var manager = new CommandManager();

        var command = new Command("and");
        manager.register(command);

        var bool = new AtomicBoolean(false);

        command.addSyntax((sender, context) -> {
            boolean bool1 = context.get("bool1");
            boolean bool2 = context.get("bool2");
            bool.set(bool1 && bool2);
        }, ArgumentType.Boolean("bool1"), ArgumentType.Boolean("bool2"));

        assertFalse(bool.get());

        manager.executeServerCommand("and true true");
        assertTrue(bool.get());

        manager.executeServerCommand("and false true");
        assertFalse(bool.get());
    }

    @Test
    public void testConditionalMessage() {
        var manager = new CommandManager();

        var command = new Command("allowed", "disallowed");
        manager.register(command);

        AtomicInteger counter = new AtomicInteger(0);

        command.setCondition((sender, commandString) -> {
            if (commandString == null) {
                return true;
            }
            var result = commandString.startsWith("allowed");
            counter.set(result ? 1 : 0);
            return result;
        });

        command.setDefaultExecutor((sender, context) -> {});

        assertEquals(0, counter.get());

        manager.executeServerCommand("allowed");
        assertEquals(1, counter.get());

        manager.executeServerCommand("disallowed");
        assertEquals(0, counter.get());
    }
}
