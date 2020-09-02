package fr.themode.demo.commands;

import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;

public class SimpleCommand implements CommandProcessor {
    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"alias"};
    }

    @Override
    public boolean process(CommandSender sender, String command, String[] args) {
        if (!sender.isPlayer())
            return false;

        sender.asPlayer().getInstance().saveChunksToStorage(() -> System.out.println("END SAVE"));

        System.gc();

        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }

    @Override
    public boolean enableWritingTracking() {
        return true;
    }

    @Override
    public String[] onWrite(String text) {
        return new String[]{"Complete1", "Complete2"};
    }
}
