package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        Argument test = ArgumentType.ItemStack("test");

        test.setCallback((source, value, error) -> {
            System.out.println("ERROR " + error);
        });

        setDefaultExecutor((source, args) -> {
            System.out.println("DEFAULT");
            System.gc();
        });

        addSyntax((source, args) -> {
            System.out.println("ARG 1");
        }, test);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
