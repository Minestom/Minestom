package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.light.LightingChunk;

public class TestLightCommand extends Command {
    public TestLightCommand() {
        super("testlight");
        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;
            if (player.getChunk() instanceof LightingChunk chunk) {
                player.sendMessage("Resend");
                chunk.scheduleFullResend();
            }
        }, ArgumentType.Literal("resendCurrentChunkLight"));
        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;
            player.sendMessage("Resend");
            player.sendPacket(player.getChunk().getFullDataPacket());
        }, ArgumentType.Literal("resendCurrentChunkDataAndLight"));
    }
}
