package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class KillCommand extends Command {
    public KillCommand() {
        super("kill");

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.kill();
            }
        });
    }
}
