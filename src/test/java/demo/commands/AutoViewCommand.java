package demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class AutoViewCommand extends Command {
    public AutoViewCommand() {
        super("autoview");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            final boolean autoView = context.get("auto-view");
            player.setAutoViewable(autoView);
            player.sendMessage("Auto view set to " + autoView);
        }, ArgumentType.Boolean("auto-view"));
    }
}
