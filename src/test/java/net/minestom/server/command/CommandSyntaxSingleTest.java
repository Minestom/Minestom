package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.String;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CommandSyntaxSingleTest {
    @Test
    public void singleInteger() {
        List<Argument<?>> args = List.of(Integer("number"));
        assertSyntax(args, "5", ExpectedExecution.SYNTAX, Map.of("number", 5));
        assertSyntax(args, "5 5", ExpectedExecution.DEFAULT);
        assertSyntax(args, "", ExpectedExecution.DEFAULT);
    }

    @Test
    public void singleIntegerInteger() {
        List<Argument<?>> args = List.of(Integer("number"), Integer("number2"));
        assertSyntax(args, "5", ExpectedExecution.DEFAULT);
        assertSyntax(args, "5 6", ExpectedExecution.SYNTAX, Map.of("number", 5, "number2", 6));
        assertSyntax(args, "", ExpectedExecution.DEFAULT);
    }

    @Test
    public void singleString() {
        List<Argument<?>> args = List.of(String("string"));
        assertSyntax(args, """
                "value"
                """, ExpectedExecution.SYNTAX, Map.of("string", "value"));
        assertSyntax(args, "5 5", ExpectedExecution.DEFAULT);
        assertSyntax(args, "", ExpectedExecution.DEFAULT);
    }

    @Test
    public void singleStringString() {
        List<Argument<?>> args = List.of(String("string"), String("string2"));
        assertSyntax(args, "test", ExpectedExecution.DEFAULT);
        assertSyntax(args, """
                "first" "second"
                """, ExpectedExecution.SYNTAX, Map.of("string", "first", "string2", "second"));
        assertSyntax(args, """
                "unescaped" "esc\\"aped"
                """, ExpectedExecution.SYNTAX, Map.of("string", "unescaped", "string2", "esc\"aped"));
        assertSyntax(args, "5 5", ExpectedExecution.SYNTAX);
        assertSyntax(args, "", ExpectedExecution.DEFAULT);
    }

    private static void assertSyntax(List<Argument<?>> args, String input, ExpectedExecution expectedExecution, Map<String, Object> expectedValues) {
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

        command.addSyntax((sender, context) -> {
            if (!result.compareAndSet(null, ExpectedExecution.SYNTAX)) {
                fail("Multiple execution: " + result.get());
            }
            values.set(context.getMap());
        }, args.toArray(Argument[]::new));

        final String executeString = commandName + " " + input;
        manager.executeServerCommand(executeString);
        assertEquals(expectedExecution, result.get());
        if (expectedValues != null) {
            assertEquals(expectedValues, values.get());
        }
    }

    private static void assertSyntax(List<Argument<?>> args, String input, ExpectedExecution expectedExecution) {
        assertSyntax(args, input, expectedExecution, null);
    }

    enum ExpectedExecution {
        DEFAULT,
        SYNTAX
    }
}
