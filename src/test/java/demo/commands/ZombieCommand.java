package demo.commands;

import demo.entity.ZombieCreature;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZombieCommand implements CommandProcessor {
    @NotNull
    @Override
    public String getCommandName() {
        return "zombie";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean process(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args) {

        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        ZombieCreature zombie = new ZombieCreature(player.getPosition());
        zombie.setInstance(player.getInstance());
        zombie.spawn();

        return true;
    }

    @Override
    public boolean hasAccess(@NotNull Player player) {
        return true;
    }
}
