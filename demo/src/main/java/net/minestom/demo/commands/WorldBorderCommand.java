package net.minestom.demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.location.RelativeVec;

public class WorldBorderCommand extends Command {
    public WorldBorderCommand() {
        super("worldborder");

        var diameterOptions = ArgumentType.Word("diameterOptions").from("set", "add"); // "center", "warning-time", "warning-distance"
        var sizeInBlocks = ArgumentType.Integer("sizeInBlocks").setDefaultValue(0);
        var timeInSeconds = ArgumentType.Double("timeInSeconds").setDefaultValue(0.0);

        var centerOption = ArgumentType.Word("centerOption").from("center");
        var centerCoordinate = ArgumentType.RelativeVec2("coordinate");

        var warningTimeOption = ArgumentType.Word("warningTimeOption").from("warning-time");

        var warningDistanceOption = ArgumentType.Word("warningDistanceOption").from("warning-distance");

        addSyntax(WorldBorderCommand::handleDiameter, diameterOptions, sizeInBlocks, timeInSeconds);
        addSyntax(WorldBorderCommand::handleCenter, centerOption, centerCoordinate);
        addSyntax(WorldBorderCommand::handleWarningTime, warningTimeOption, timeInSeconds);
        addSyntax(WorldBorderCommand::handleWarningDistance, warningDistanceOption, sizeInBlocks);
    }

    private static void handleDiameter(CommandSender source, CommandContext context) {
        Player player = (Player) source;
        int size = context.get("sizeInBlocks");
        double timeInSeconds = context.get("timeInSeconds");
        double diameter = size;
        if (context.get("diameterOptions").equals("add")) {
            diameter += player.getInstance().getWorldBorder().diameter();
        }

        player.getInstance().setWorldBorder(player.getInstance().getWorldBorder().withDiameter(diameter), timeInSeconds);
    }

    private static void handleCenter(CommandSender source, CommandContext context) {
        Player player = (Player) source;
        RelativeVec coords = context.get("coordinate");
        Vec vec = coords.from(new Pos(0, 0, 0));
        player.getInstance().setWorldBorder(player.getInstance().getWorldBorder().withCenter(vec.x(), vec.z()));
    }

    private static void handleWarningTime(CommandSender source, CommandContext context) {
        Player player = (Player) source;
        double timeInSeconds = context.get("timeInSeconds");
        player.getInstance().setWorldBorder(player.getInstance().getWorldBorder().withWarningTime((int)timeInSeconds));
    }

    private static void handleWarningDistance(CommandSender source, CommandContext context) {
        Player player = (Player) source;
        int sizeInBlocks = context.get("sizeInBlocks");
        player.getInstance().setWorldBorder(player.getInstance().getWorldBorder().withWarningDistance(sizeInBlocks));
    }
}
