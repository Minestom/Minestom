package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            player.startConfigurationPhase();
        });
    }
}
