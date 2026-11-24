package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            public void globalListener(CommandSender sender, CommandContext context, String command) {
                hasRun.set(true);
                context.setArg("key", "value", "value");
            }
        };

        manager.register(command);

        AtomicBoolean checkSet = new AtomicBoolean(false);
        command.setDefaultExecutor((_, context) -> checkSet.set("value".equals(context.get("key"))));

        manager.executeServerCommand("command");

        assertTrue(hasRun.get());
        assertTrue(checkSet.get());

    }

    @Test
    public void testSyntaxesTreeUnwrapsArguments() {
        var literal = syntaxesTree(ArgumentType.Literal("sub"));
        assertEquals(literal, syntaxesTree(ArgumentType.Literal("sub").map(Function.identity())));
        assertEquals(literal, syntaxesTree(ArgumentType.Literal("sub").filter(_ -> true)));

        var restrictedWord = syntaxesTree(ArgumentType.Word("word").from("a", "b"));
        assertEquals(restrictedWord, syntaxesTree(ArgumentType.Word("word").from("a", "b").map(Function.identity())));
        assertEquals(restrictedWord, syntaxesTree(ArgumentType.Word("word").from("a", "b").filter(_ -> true)));
    }

    private static String syntaxesTree(Argument<?> argument) {
        var command = new Command("command");
        command.addSyntax((_, _) -> {}, argument, ArgumentType.Integer("number"));
        return command.getSyntaxesTree();
    }
}
