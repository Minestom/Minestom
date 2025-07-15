package net.minestom.demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.jspecify.annotations.Nullable;

public class LegacyCommand extends net.minestom.server.command.builder.SimpleCommand {
    public LegacyCommand() {
        super("test", "alias");
    }

    @Override
    public boolean process(CommandSender sender, String command, String[] args) {
        if (!(sender instanceof Player)) return false;

        System.gc();
        sender.sendMessage("Explicit GC");
        return true;
    }

    @Override
    public boolean hasAccess(CommandSender sender, @Nullable String commandString) {
        return true;
    }
}
