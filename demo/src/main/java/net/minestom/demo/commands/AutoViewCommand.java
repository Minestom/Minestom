package net.minestom.demo.commands;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Boolean;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class AutoViewCommand extends Command {
    public AutoViewCommand() {
        super("autoview");

        setCondition(Conditions::playerOnly);
        setDefaultExecutor(CommandManager.STANDARD_DEFAULT_EXECUTOR);

        var value = Boolean("value").setCallback(CommandException.STANDARD_CALLBACK);

        // Modify viewable
        addSyntax((origin, context) -> {
            if (!(origin.entity() instanceof Player player)) return;
            final boolean autoView = context.get("value");
            player.setAutoViewable(autoView);
            player.sendMessage("Auto-viewable set to " + autoView);
        }, Literal("viewable").setCallback(CommandException.STANDARD_CALLBACK), value);

        // Modify viewer
        addSyntax((origin, context) -> {
            if (!(origin.entity() instanceof Player player)) return;
            final boolean autoView = context.get("value");
            player.setAutoViewEntities(autoView);
            player.sendMessage("Auto-viewer set to " + autoView);
        }, Literal("viewer").setCallback(CommandException.STANDARD_CALLBACK), value);

        // Modify viewable rule
        addSyntax((origin, context) -> {
            if (!(origin.entity() instanceof Player player)) return;
            EntityFinder finder = context.get("targets");
            final List<Entity> entities = finder.find(origin.sender());
            player.updateViewableRule(entities::contains);
            player.sendMessage("Viewable rule updated to see " + entities.size() + " players");
        }, Literal("rule-viewable").setCallback(CommandException.STANDARD_CALLBACK), Entity("targets").onlyPlayers(true).setCallback(CommandException.STANDARD_CALLBACK));

        // Modify viewer rule
        addSyntax((origin, context) -> {
            if (!(origin.entity() instanceof Player player)) return;
            EntityFinder finder = context.get("targets");
            final List<Entity> entities = finder.find(origin.sender());
            player.updateViewerRule(entities::contains);
            player.sendMessage("Viewer rule updated to see " + entities.size() + " entities");
        }, Literal("rule-viewer").setCallback(CommandException.STANDARD_CALLBACK), Entity("targets").setCallback(CommandException.STANDARD_CALLBACK));

        // Remove viewable rule
        addSyntax((origin, context) -> {
            if (!(origin.entity() instanceof Player player)) return;
            player.updateViewableRule(p -> true);
            player.sendMessage("Viewable rule removed");
        }, Literal("remove-rule-viewable").setCallback(CommandException.STANDARD_CALLBACK));

        // Remove viewer rule
        addSyntax((origin, context) -> {
            if (!(origin.entity() instanceof Player player)) return;
            player.updateViewerRule(p -> true);
            player.sendMessage("Viewer rule removed");
        }, Literal("remove-rule-viewer").setCallback(CommandException.STANDARD_CALLBACK));

        // Update viewable rule
        addSyntax((origin, context) -> {
            if (!(origin.entity() instanceof Player player)) return;
            player.updateViewableRule();
            player.sendMessage("Viewable rule updated");
        }, Literal("update-rule-viewable").setCallback(CommandException.STANDARD_CALLBACK));

        // Update viewer rule
        addSyntax((origin, context) -> {
            if (!(origin.entity() instanceof Player player)) return;
            player.updateViewerRule();
            player.sendMessage("Viewer rule updated");
        }, Literal("update-rule-viewer").setCallback(CommandException.STANDARD_CALLBACK));
    }
}
