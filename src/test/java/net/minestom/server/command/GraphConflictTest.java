package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.junit.jupiter.api.Test;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GraphConflictTest {
    @Test
    public void sameLiterals() {
        final Command foo = new Command("foo");
        var first = Literal("first");
        foo.addSyntax(GraphConflictTest::dummyExecutor, first);
        foo.addSyntax(GraphConflictTest::dummyExecutor, first);
        assertThrows(Exception.class, () -> Graph.fromCommand(foo));
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
