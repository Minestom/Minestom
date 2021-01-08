package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class TabCompleteListener {

    private static final CommandManager COMMAND_MANAGER = MinecraftServer.getCommandManager();

    public static void listener(ClientTabCompletePacket packet, Player player) {
        final String text = packet.text;

        final String[] split = packet.text.split(Pattern.quote(StringUtils.SPACE));

        final String commandName = split[0].replaceFirst(CommandManager.COMMAND_PREFIX, "");

        // Tab complete for CommandProcessor
        final CommandProcessor commandProcessor = COMMAND_MANAGER.getCommandProcessor(commandName);
        if (commandProcessor != null) {
            final int start = findStart(text, split);
            final String[] matches = commandProcessor.onWrite(player, text);
            if (matches != null && matches.length > 0) {
                sendTabCompletePacket(packet.transactionId, start, matches, player);
            }
        } else {
            // Tab complete for Command
            final Command command = COMMAND_MANAGER.getCommand(commandName);
            if (command != null) {
                final int start = findStart(text, split);
                final String[] matches = command.onDynamicWrite(player, text);
                if (matches != null && matches.length > 0) {
                    sendTabCompletePacket(packet.transactionId, start, matches, player);
                }
            }
        }


    }

    private static int findStart(String text, String[] split) {
        final boolean endSpace = text.endsWith(StringUtils.SPACE);
        int start;
        if (endSpace) {
            start = text.length();
        } else {
            final String lastArg = split[split.length - 1];
            start = text.lastIndexOf(lastArg);
        }
        return start;
    }

    private static void sendTabCompletePacket(int transactionId, int start, String[] matches, Player player) {
        TabCompletePacket tabCompletePacket = new TabCompletePacket();
        tabCompletePacket.transactionId = transactionId;
        tabCompletePacket.start = start;
        tabCompletePacket.length = 20;

        TabCompletePacket.Match[] matchesArray = new TabCompletePacket.Match[matches.length];
        for (int i = 0; i < matchesArray.length; i++) {
            TabCompletePacket.Match match = new TabCompletePacket.Match();
            match.match = matches[i];
            matchesArray[i] = match;
        }

        tabCompletePacket.matches = matchesArray;

        player.getPlayerConnection().sendPacket(tabCompletePacket);
    }


}
