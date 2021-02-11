package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandResult;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        addSyntax((sender, args) -> {
            final CommandResult result = args.get("command");
            System.out.println("test " + result.getType() + " " + result.getInput());
        }, Literal("cmd"), Command("command"));

        addSyntax((sender, args) -> {
            List<Arguments> groups = args.get("groups");
            System.out.println("size " + groups.size());
        }, Literal("loop"), Loop("groups",
                Group("group", Literal("name"), Word("word1")),
                Group("group2", Literal("delay"), Integer("number2"))));
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
