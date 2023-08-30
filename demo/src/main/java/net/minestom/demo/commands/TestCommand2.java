package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;

public class TestCommand2 extends Command {
    public TestCommand2() {
        super("test2");

        var argA = ArgumentType.String("a");
        var argB = ArgumentType.String("b");

        addSyntax((sender, context) -> {
            sender.sendMessage("a only");
        }, argA);
        addSyntax((sender, context) -> {
            sender.sendMessage("a and b");
        }, argB, argA);
    }
}
