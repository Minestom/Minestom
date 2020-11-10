package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.item.ItemStack;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);
        {
            //Argument dynamicWord = ArgumentType.DynamicWord("test");

            //addSyntax(this::execute, dynamicWord);
        }

        Argument test = ArgumentType.ItemStack("item");

        test.setCallback((source, value, error) -> {
            System.out.println("ERROR " + error);
        });

        setDefaultExecutor((source, args) -> {
            System.out.println("DEFAULT");
            System.gc();
        });

        addSyntax((source, args) -> {
            ItemStack itemStack = args.getItemStack("item");
            System.out.println("HEY IT WORKS "+itemStack.getMaterial());
        }, test);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
