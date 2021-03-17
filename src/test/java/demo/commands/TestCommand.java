package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        var test1 = Word("msg").setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("test"));
        });

        var test2 = String("msg2").setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("greer"));
        });

        addSyntax((sender, context) -> {
            System.out.println("executed");
        }, Literal("test"), test1, test2);

        addSyntax((sender, context) -> {
            System.out.println("cmd syntax");
        }, Literal("debug"), Command("cmd").setShortcut("testcmd test"));

    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage("Incorrect usage");
    }

}
