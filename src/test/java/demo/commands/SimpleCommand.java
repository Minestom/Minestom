package demo.commands;

import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SimpleCommand implements CommandProcessor {
    @NotNull
    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"alias"};
    }

    @Override
    public boolean process(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args) {
        if (!sender.isPlayer())
            return false;

        sender.asPlayer().getInstance().saveChunksToStorage(() -> System.out.println("END SAVE"));

        System.gc();

        return true;
    }

    @Override
    public boolean hasAccess(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean enableWritingTracking() {
        return true;
    }

    @Override
    public String[] onWrite(@NotNull CommandSender sender, @NotNull String text) {
        return new String[]{"Complete1", "Complete2"};
    }
}
