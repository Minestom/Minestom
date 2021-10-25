package demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Boolean;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class AutoViewCommand extends Command {
    public AutoViewCommand() {
        super("autoview");

        // Modify viewing state
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            final boolean autoView = context.get("auto-view");
            player.setAutoViewable(autoView);
            player.sendMessage("Auto view set to " + autoView);
        }, Literal("set"), Boolean("auto-view"));

        // Modify viewing rule
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            EntityFinder finder = context.get("targets");
            final List<Entity> entities = finder.find(sender);
            player.updateViewingRule(entities::contains);
            player.sendMessage("Rule updated to see " + entities.size() + " players");
        }, Literal("rule"), Entity("targets").onlyPlayers(true));

        // Remove viewing rule
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            player.updateViewingRule(null);
            player.sendMessage("Rule removed");
        }, Literal("remove-rule"));
    }
}
