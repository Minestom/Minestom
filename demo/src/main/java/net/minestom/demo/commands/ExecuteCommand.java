package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;

public class ExecuteCommand extends Command {

    public ExecuteCommand() {
        super("execute");
        ArgumentLiteral run = new ArgumentLiteral("run");
        run.setRedirectTarget(new String[0]);

        addSyntax(((sender, context) -> {}), run);
    }
}
