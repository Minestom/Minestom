package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class PingCommand extends Command {
    public PingCommand() {
        super("ping");
        addSyntax((source, context) -> {
            Player player = (Player) source;
            int latency = player.getLatency();
            String color = "§a";
            if (latency >= 200) color = "§4";
            else if (latency >= 150) color = "§c";
            else if (latency >= 100) color = "§6";
            else if (latency >= 50) color = "§e";
            player.sendMessage(String.format("§7Ping: %s%d§7ms", color, latency));
        });
    }
}
