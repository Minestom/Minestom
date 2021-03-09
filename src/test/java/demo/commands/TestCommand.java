package demo.commands;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;

import static net.minestom.server.command.builder.arguments.ArgumentType.Component;
import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        var number = Integer("number2");

        addSyntax((sender, args) -> {
            sender.sendMessage((JsonMessage) args.get("msg"));
        }, Component("msg"));
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
