package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.*;
import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class CommandSuggestionSubcommandTest {

    /**
     * Make sure that when we have a {@code /foo bar} and {@code /foo baz}, we use the correct default executor within
     * the command chain
     */
    @Test
    public void useProperDefaultExecutor() {
        var manager = new CommandManager();
        var command = new Command("foo");
        var barCommand = new Command("bar");
        var bazCommand = new Command("baz");

        var wordArg1 = Word("wordArg1");
        var wordArg2 = Word("wordArg2");

        command.setDefaultExecutor((sender, context) -> {
            // Since baz has a default executor, we shouldn't be calling this
            fail("Command executor should not have been called");
        });

        barCommand.setDefaultExecutor((sender, context) -> {
            // This should never be called, original behaviour had this happen due to malformed command chain
            fail("Bar subcommand executor should not have been called");
        });

        // This is the default executor we're expecting to call
        bazCommand.setDefaultExecutor((sender, context) -> {});
        bazCommand.addSyntax((sender, context) -> {}, wordArg1, wordArg2);

        command.addSubcommand(barCommand);
        command.addSubcommand(bazCommand);
        manager.register(command);

        // Failing execution for the baz subcommand should not be calling any other default executor if itself has one
        // if we just did 'foo baz' this would work, however would call the wrong default executor (the one belonging to
        // bar) when we only provided a subset of the arguments rather than all.
        manager.executeServerCommand("foo baz test");
    }

    /**
     * Make sure than when we have a {@code /foo} command, and we enter incorrect
     * arguments it defaults to the correct default executor
     */
    @Test
    public void useCorrectDefaultExecutor() {
        var manager = new CommandManager();
        var command = new Command("foo");
        var barCommand = new Command("bar");
        var bazCommand = new Command("baz", "bom");

        var wordArg1 = Word("wordArg1");
        var wordArg2 = Word("wordArg2");

        bazCommand.setDefaultExecutor((sender, context) -> {
            // Since the base command has a default executor, we shouldn't be calling this
            fail("Baz subcommand command executor should not have been called");
        });

        barCommand.setDefaultExecutor((sender, context) -> {
            // This should never be called, original behaviour had this happen due to malformed command chain
            fail("Bar subcommand executor should not have been called");
        });

        // This is the default executor we're expecting to call
        command.setDefaultExecutor((sender, context) -> {});
        bazCommand.addSyntax((sender, context) -> {}, wordArg1, wordArg2);

        command.addSubcommand(barCommand);
        command.addSubcommand(bazCommand);
        manager.register(command);

        // Failing this means that the base command is defaulting to the incorrect executor
        // in the chain, it should be using the own default executor but its using one of the
        // subcommands's default executors
        manager.executeServerCommand("foo abc");
    }
}
