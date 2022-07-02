package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import org.junit.jupiter.api.Test;

import java.lang.String;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.minestom.server.command.builder.arguments.ArgumentType.Boolean;
import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.String;
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
        }, Boolean("bool1"), Boolean("bool2"));

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

        command.setDefaultExecutor((sender, context) -> {
        });

        assertEquals(0, counter.get());

        manager.executeServerCommand("allowed");
        assertEquals(1, counter.get());

        manager.executeServerCommand("disallowed");
        assertEquals(0, counter.get());
    }

    @Test
    public void singleInteger() {
        List<Argument<?>> args = List.of(Integer("number"));
        assertSingleSyntax(args, "5", ExpectedExecution.FIRST_SYNTAX, Map.of("number", 5));
        assertSingleSyntax(args, "5 5", ExpectedExecution.DEFAULT);
        assertSingleSyntax(args, "", ExpectedExecution.DEFAULT);
    }

    @Test
    public void singleString() {
        List<Argument<?>> args = List.of(String("string"));
        assertSingleSyntax(args, """
                "value"
                """, ExpectedExecution.FIRST_SYNTAX, Map.of("string", "value"));
        assertSingleSyntax(args, "5 5", ExpectedExecution.DEFAULT);
        assertSingleSyntax(args, "", ExpectedExecution.DEFAULT);
    }

    private static void assertSyntax(List<List<Argument<?>>> args, String input, ExpectedExecution expectedExecution, Map<String, Object> expectedValues) {
        final String commandName = "name";

        var manager = new CommandManager();
        var command = new Command(commandName);
        manager.register(command);

        AtomicReference<ExpectedExecution> result = new AtomicReference<>();
        AtomicReference<Map<String, Object>> values = new AtomicReference<>();

        command.setDefaultExecutor((sender, context) -> {
            if (!result.compareAndSet(null, ExpectedExecution.DEFAULT)) {
                fail("Multiple execution: " + result.get());
            }
        });

        int i = ExpectedExecution.FIRST_SYNTAX.ordinal();
        for (List<Argument<?>> t : args) {
            ExpectedExecution id = ExpectedExecution.values()[i++];
            command.addSyntax((sender, context) -> {
                if (!result.compareAndSet(null, id)) {
                    fail("Multiple execution: " + result.get());
                }
                values.set(context.getMap());
            }, t.toArray(Argument[]::new));
        }

        final String executeString = commandName + " " + input;
        manager.executeServerCommand(executeString);
        assertEquals(expectedExecution, result.get());
        if (expectedValues != null) {
            assertEquals(expectedValues, values.get());
        }
    }

    private static void assertSyntax(List<List<Argument<?>>> args, String input, ExpectedExecution expectedExecution) {
        assertSyntax(args, input, expectedExecution, null);
    }

    private static void assertSingleSyntax(List<Argument<?>> args, String input, ExpectedExecution expectedExecution, Map<String, Object> expectedValues) {
        assertSyntax(List.of(args), input, expectedExecution, expectedValues);
    }

    private static void assertSingleSyntax(List<Argument<?>> args, String input, ExpectedExecution expectedExecution) {
        assertSingleSyntax(args, input, expectedExecution, null);
    }

    enum ExpectedExecution {
        DEFAULT,

        FIRST_SYNTAX,
        SECOND_SYNTAX,
        THIRD_SYNTAX
    }
}
