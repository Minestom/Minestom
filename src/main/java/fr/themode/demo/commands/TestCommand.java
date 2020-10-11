package fr.themode.demo.commands;

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
        {
            //Argument dynamicWord = ArgumentType.DynamicWord("test");

            //addSyntax(this::execute, dynamicWord);
        }

        Argument test = ArgumentType.Word("test").from("hey");
        Argument num = ArgumentType.Integer("num");
        Argument test2 = ArgumentType.Word("test2").from("oh");
        Argument num2 = ArgumentType.Float("num2");

        addSyntax((source, args) -> {
        }, test, num);
        addSyntax((source, args) -> {
        }, test2, num2);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }

    private void execute(CommandSender sender, Arguments arguments) {
        final String word = arguments.getWord("test");
        sender.sendMessage("word: " + word);
    }

    private boolean isAllowed(Player player) {
        return true; // TODO: permissions
    }

    @Override
    public String[] onDynamicWrite(String text) {
        return new String[]{"test1", "test2"};
    }
}
