package demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        var block = ArgumentType.BlockState("block");
        block.setCallback((origin, exception) -> exception.printStackTrace());

        addSyntax((origin, context) -> System.out.println("executed"), block);
    }

    private void usage(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        origin.sender().sendMessage(Component.text("Incorrect usage"));
    }

}
