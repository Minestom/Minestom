package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

import java.util.Collection;

import static net.minestom.server.command.builder.arguments.ArgumentType.Float;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class FindCommand extends Command {
    public FindCommand() {
        super("find");

        setCondition(Conditions::playerOnly);

        this.addSyntax((origin, context) -> {
            Player player = (Player) origin.sender();
            float range = context.get("range");

            Collection<Entity> entities = player.getInstance().getNearbyEntities(player.getPosition(), range);

            player.sendMessage("Search result: ");

            for (Entity entity : entities) {
                player.sendMessage("    " + entity.getEntityType() + ": ");
                player.sendMessage("        Meta: " + entity.getEntityMeta());
                player.sendMessage("        Permissions: " + entity.getAllPermissions());
                player.sendMessage("        Position: " + entity.getPosition());
            }

            player.sendMessage("End result.");
        }, Literal("entity"), Float("range"));
    }
}
