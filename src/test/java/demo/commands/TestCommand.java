package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        Argument test = ArgumentType.ItemStack("test");

        test.setCallback((source, value, error) -> {
            System.out.println("ERROR " + error);
        });

        setDefaultExecutor((source, args) -> {
            System.gc();
            source.sendMessage("Explicit GC executed!");
        });

        addSyntax((source, args) -> {
            Player player = (Player) source;
            System.out.println("ARG 1");
        }, test);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
