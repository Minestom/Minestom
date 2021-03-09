package demo.commands;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.minestom.server.command.builder.arguments.ArgumentType.String;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        addSyntax((sender, args) -> {
            System.out.println("test: " + args.get("msg"));
        }, String("msg").setSuggestionCallback((suggestion, input) -> {
            suggestion.addEntry(new SuggestionEntry(input, ColoredText.of(ChatColor.RED, "Hover")));
        }));
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
