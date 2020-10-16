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

        Argument test = ArgumentType.ItemStack("test");

        addSyntax((source, args) -> {
            System.out.println("SUCCESS");
            ItemStack itemStack = args.getItemStack("test");
            source.asPlayer().getInventory().addItemStack(itemStack);
        }, test);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
