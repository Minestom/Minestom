package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
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
    public void testCommandCondition() {
        var manager = new CommandManager();

        var command = new Command("name");

        AtomicBoolean hasFailedCondition = new AtomicBoolean(false);
        AtomicBoolean hasExecuted = new AtomicBoolean(false);

        command.setCondition((sender, commandString) -> {
            hasFailedCondition.set(true);
            return false;
        });
        command.setDefaultExecutor((sender, context) -> hasExecuted.set(true));
        manager.register(command);

        // Should trigger the condition but not the executor
        manager.executeServerCommand("name");

        assertTrue(hasFailedCondition.get());
        assertFalse(hasExecuted.get());
    }

    @Test
    public void testSubCommands() {
        var manager = new CommandManager();

        var parent = new Command("parent");
        var child = new Command("child");

        parent.addSubcommand(child);
        manager.register(parent);

        AtomicBoolean parentExecuted = new AtomicBoolean(false);
        AtomicBoolean childExecuted = new AtomicBoolean(false);

        parent.setDefaultExecutor((sender, context) -> parentExecuted.set(true));
        child.setDefaultExecutor((sender, context) -> childExecuted.set(true));

        manager.executeServerCommand("parent child");

        assertFalse(parentExecuted.get());
        assertTrue(childExecuted.get());
    }

    @Test
    public void testSubCommandConditions() {
        var manager = new CommandManager();

        var parent = new Command("parent");
        var child = new Command("child");

        parent.addSubcommand(child);
        manager.register(parent);

        AtomicBoolean parentConditionTriggered = new AtomicBoolean(false);
        AtomicBoolean childConditionTriggered = new AtomicBoolean(false);
        AtomicBoolean parentExecuted = new AtomicBoolean(false);
        AtomicBoolean childExecuted = new AtomicBoolean(false);

        parent.setCondition((sender, commandString) -> {
            parentConditionTriggered.set(true);
            return true; // Return true so the child's condition has a chance to get tested
        });
        child.setCondition((sender, commandString) -> {
            childConditionTriggered.set(true);
            return false;
        });
        parent.setDefaultExecutor((sender, context) -> parentExecuted.set(true));
        child.setDefaultExecutor((sender, context) -> childExecuted.set(true));

        manager.executeServerCommand("parent child");

        assertTrue(parentConditionTriggered.get());
        assertTrue(childConditionTriggered.get());
        assertFalse(parentExecuted.get());
        assertFalse(childExecuted.get());
    }

}
