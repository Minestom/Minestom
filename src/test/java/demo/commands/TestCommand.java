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

        var test = ResourceLocation("msg");

        addSyntax((sender, context) -> {
            System.out.println("executed");
        },test);
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage("Incorrect usage");
    }

}
