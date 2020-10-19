package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentNbtTag;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);
        {
            //Argument dynamicWord = ArgumentType.DynamicWord("test");

            //addSyntax(this::execute, dynamicWord);
        }

        Argument test = new ArgumentNbtTag("test");

        addSyntax((source, args) -> {
            System.out.println("arg: "+args.getNBT("test").getClass());
            System.out.println("SUCCESS");
        }, test);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
