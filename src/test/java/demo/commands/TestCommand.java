package demo.commands;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        var test1 = Word("msg").setSuggestionCallback((suggestion, input) -> {
            suggestion.addEntry(new SuggestionEntry("sug1", ColoredText.of(ChatColor.RED, "Hover")));
            suggestion.addEntry(new SuggestionEntry("sug2"));
        });

        var test2 = Word("msg2").setSuggestionCallback((suggestion, input) -> {
            suggestion.addEntry(new SuggestionEntry(input, ColoredText.of(ChatColor.BRIGHT_GREEN, "GHRTEG")));
        });

        var test3 = Integer("msg3");

        addSyntax((sender, context) -> {
            System.out.println("input: "+context.getInput());
        }, test3, test1);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
