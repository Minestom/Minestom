package demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import static net.minestom.server.command.builder.arguments.ArgumentType.Boolean;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class AutoViewCommand extends Command {
    public AutoViewCommand() {
        super("autoview");

        // Modify viewable
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            final boolean autoView = context.get("value");
            player.setAutoViewable(autoView);
            player.sendMessage("Auto-viewable set to " + autoView);
        }, Literal("viewable"), Boolean("value"));

        // Modify viewer
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            final boolean autoView = context.get("value");
            player.setAutoViewer(autoView);
            player.sendMessage("Auto-viewer set to " + autoView);
        }, Literal("viewer"), Boolean("value"));
    }
}
