package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A simple shutdown command.
 */
public class ShutdownCommand implements CommandProcessor {

    @NotNull
    @Override
    public String getCommandName() {
        return "shutdown";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean process(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args) {
        MinecraftServer.stopCleanly();
        return true;
    }

    @Override
    public boolean hasAccess(@NotNull Player player) {
        return true;
    }
}
