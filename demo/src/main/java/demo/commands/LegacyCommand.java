package demo.commands;

import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LegacyCommand extends net.minestom.server.command.builder.SimpleCommand {
    public LegacyCommand() {
        super("test", "alias");
    }

    @Override
    public boolean process(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args) {
        if (!sender.isPlayer())
            return false;

        System.gc();
        sender.sendMessage("Explicit GC");
        return true;
    }

    @Override
    public boolean hasAccess(@NotNull CommandSender sender, @Nullable String commandString) {
        return true;
    }
}
