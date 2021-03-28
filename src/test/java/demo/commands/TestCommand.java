package demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;

import static net.minestom.server.command.builder.arguments.ArgumentType.ResourceLocation;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        var test = ResourceLocation("msg");

        addSyntax((sender, context) -> System.out.println("executed"),test);
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Incorrect usage"));
    }

}
