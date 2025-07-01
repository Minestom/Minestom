package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.BlockBatch;
import net.minestom.server.instance.Instance;

import static net.minestom.server.command.builder.arguments.ArgumentType.RelativeBlockPosition;

public class BatchCommand extends Command {
    public BatchCommand() {
        super("batch");

        final ArgumentRelativeBlockPosition start = RelativeBlockPosition("start");
        final ArgumentRelativeBlockPosition end = RelativeBlockPosition("end");
        final ArgumentRelativeBlockPosition target = RelativeBlockPosition("target");

        addSyntax((sender, context) -> {
            final Player player = (Player) sender;

            final Vec startVec = context.get(start).from(player);
            final Vec endVec = context.get(end).from(player);
            final Vec targetVec = context.get(target).from(player);

            Instance instance = player.getInstance();

            // Calculate block count
            final int minX = Math.min((int) startVec.x(), (int) endVec.x());
            final int maxX = Math.max((int) startVec.x(), (int) endVec.x());
            final int minY = Math.min((int) startVec.y(), (int) endVec.y());
            final int maxY = Math.max((int) startVec.y(), (int) endVec.y());
            final int minZ = Math.min((int) startVec.z(), (int) endVec.z());
            final int maxZ = Math.max((int) startVec.z(), (int) endVec.z());

            int blockCount = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);

            // Measure getBlockBatch timing
            long getStartTime = System.nanoTime();
            final BlockBatch batch = instance.getBlockBatch(Vec.ZERO, startVec, endVec);
            long getEndTime = System.nanoTime();
            double getTimeMs = (getEndTime - getStartTime) / 1_000_000.0;

            // Measure setBlockBatch timing
            long setStartTime = System.nanoTime();
            instance.setBlockBatch(targetVec, batch);
            long setEndTime = System.nanoTime();
            double setTimeMs = (setEndTime - setStartTime) / 1_000_000.0;

            // Send formatted message to player
            Component message = Component.text()
                    .append(Component.text("Batch operation completed", NamedTextColor.GREEN, TextDecoration.BOLD))
                    .append(Component.newline())
                    .append(Component.text("• ", NamedTextColor.GRAY))
                    .append(Component.text("Block count: ", NamedTextColor.WHITE))
                    .append(Component.text(String.format("%,d blocks", blockCount), NamedTextColor.AQUA, TextDecoration.BOLD))
                    .append(Component.newline())
                    .append(Component.text("• ", NamedTextColor.GRAY))
                    .append(Component.text("Get batch: ", NamedTextColor.WHITE))
                    .append(Component.text(String.format("%.2fms", getTimeMs), NamedTextColor.YELLOW))
                    .append(Component.newline())
                    .append(Component.text("• ", NamedTextColor.GRAY))
                    .append(Component.text("Set batch: ", NamedTextColor.WHITE))
                    .append(Component.text(String.format("%.2fms", setTimeMs), NamedTextColor.YELLOW))
                    .append(Component.newline())
                    .append(Component.text("• ", NamedTextColor.GRAY))
                    .append(Component.text("Total: ", NamedTextColor.WHITE))
                    .append(Component.text(String.format("%.2fms", getTimeMs + setTimeMs), NamedTextColor.GOLD, TextDecoration.BOLD))
                    .build();

            player.sendMessage(message);
        }, start, end, target);
    }
}
