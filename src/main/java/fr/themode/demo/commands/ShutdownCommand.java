package fr.themode.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;

/**
 * A simple shutdown command
 */
public class ShutdownCommand implements CommandProcessor {

    @Override
    public String getCommandName() {
        return "shutdown";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean process(CommandSender sender, String command, String[] args) {
        MinecraftServer.stopCleanly();
        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return player.getPermissionLevel() >= 4;
    }
}
