package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import org.junit.jupiter.api.Test;

import java.lang.String;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static net.minestom.server.command.builder.arguments.ArgumentType.Float;
import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CommandSyntaxMultiTest {

    @Test
    public void integerFloat() {
        List<List<Argument<?>>> args = List.of(
                List.of(Literal("integer"), Integer("number")),
                List.of(Literal("float"), Float("number"))
        );
        assertSyntax(args, "integer 5", ExpectedExecution.FIRST_SYNTAX, Map.of("integer", "integer", "number", 5));
        assertSyntax(args, "float 5.5", ExpectedExecution.SECOND_SYNTAX, Map.of("float", "float", "number", 5.5f));
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

    enum ExpectedExecution {
        DEFAULT,

        FIRST_SYNTAX,
        SECOND_SYNTAX,
        THIRD_SYNTAX
    }
}
