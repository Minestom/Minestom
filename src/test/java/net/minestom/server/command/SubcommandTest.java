package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.exception.IllegalCommandStructureException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class SubcommandTest {

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

    @Test
    public void testRecursionDetection() {
        final Command foo = new Command("foo");
        final Command bar = new Command("bar");
        bar.addSubcommand(foo);
        assertDoesNotThrow(() -> GraphBuilder.forServer(Set.of(foo, bar)));
        foo.addSubcommand(bar);
        assertTimeout(Duration.ofSeconds(5), () -> assertThrows(IllegalCommandStructureException.class,
                () -> GraphBuilder.forServer(Set.of(foo, bar)), "Builder didn't detect infinite recursion."),
                "Is your stack fine?!");
    }

}
