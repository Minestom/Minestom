package net.minestom.demo.commands;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.task.PathfindTask;
import net.minestom.server.entity.pathfinding.task.StaticMovementTask;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class PathfindCommand extends Command {

    public PathfindCommand() {
        super("pathfind");

        addSyntax(
                this::usageE2E,
                Entity("from"),
                Entity("to"),
                Word("task").from("walk", "fly")
        );

        addSyntax(
                this::usageAllToMeThenRemove,
                Literal("allToMeThenRemove")
        );

        addSyntax(
                this::legacyAllToMeThenRemove,
                Literal("legacy"),
                Literal("allToMeThenRemove")
        );
    }

    private void usageE2E(CommandSender sender, CommandContext context) {
        EntityFinder from = context.get("from");
        EntityFinder to = context.get("to");
        String task = context.get("task");

        List<Entity> fromList = from.find(sender);
        Entity toEntity = to.findFirstEntity(sender);

        if (toEntity == null) {
            sender.sendMessage("No entity found");
            return;
        }

        Pos destination = toEntity.getPosition();
        destination = destination.add(toEntity.getBoundingBox().relativeStart());

        StaticMovementTask movementTask = switch (task) {
            case "walk" -> PathfindTask.walkTo(destination, Attribute.MOVEMENT_SPEED.defaultValue());
            case "fly" -> PathfindTask.flyTo(destination, Attribute.MOVEMENT_SPEED.defaultValue());
            default -> throw new IllegalArgumentException("Unknown task: " + task);
        };

        for (Entity fromEntity : fromList) {
            if (fromEntity instanceof EntityCreature creature) {
                sender.sendMessage("Pathfinding from " + fromEntity + " to " + toEntity);
                creature.schedulePathfind(movementTask);
            }
        }
    }

    private void usageAllToMeThenRemove(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Entity entity)) {
            sender.sendMessage("You must be an entity to use this command");
            return;
        }

        Pos destination = entity.getPosition();
        destination = destination.add(entity.getBoundingBox().relativeStart());
        StaticMovementTask task = PathfindTask.walkTo(destination, Attribute.MOVEMENT_SPEED.defaultValue());

        for (Entity aEntity : entity.getInstance().getEntities()) {
            if (aEntity instanceof EntityCreature creature) {
                var execution = creature.schedulePathfind(task);
                execution.completion().thenRun(aEntity::remove);
            }
        }
    }

    private void legacyAllToMeThenRemove(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Entity entity)) {
            sender.sendMessage("You must be an entity to use this command");
            return;
        }

        Pos destination = entity.getPosition();
        destination = destination.add(
                entity.getBoundingBox().width() / 2D,
                0,
                entity.getBoundingBox().depth() / 2D
        );

        for (Entity aEntity : entity.getInstance().getEntities()) {
            if (aEntity instanceof EntityCreature creature) {
                var execution = creature.getNavigator().setPathTo(destination);
                execution.thenRun(aEntity::remove);
            }
        }
    }
}