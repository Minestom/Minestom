package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        addSyntax((sender, context) -> System.out.println("executed"), "test Get integer<number>");
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Incorrect usage"));
    }

}
