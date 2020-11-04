package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);
        {
            //Argument dynamicWord = ArgumentType.DynamicWord("test");

            //addSyntax(this::execute, dynamicWord);
        }

        Argument test = ArgumentType.Integer("number");

        setDefaultExecutor((source, args) -> {
            System.out.println("DEFAULT");
            System.gc();
        });

        addSyntax((source, args) -> {
            int number = args.getInteger("number");
            source.sendMessage("set view to " + number);
            MinecraftServer.setEntityViewDistance(number);
        }, test);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
