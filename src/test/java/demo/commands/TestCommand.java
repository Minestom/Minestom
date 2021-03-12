package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        var test1 = Integer("msg").setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("test"));
        });

        addSyntax((sender, context) -> {
            sender.sendMessage("no argument syntax");
        });

        addSyntax((sender, context) -> {
            System.out.println("executed");
        }, test1);

    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage("Incorrect usage");
    }

}
