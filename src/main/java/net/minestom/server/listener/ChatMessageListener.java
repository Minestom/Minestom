package net.minestom.server.listener;

import club.thectm.minecraft.text.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.Chat;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerChatEvent;
import net.minestom.server.event.PlayerCommandEvent;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;

import java.util.function.Function;

public class ChatMessageListener {

    public static void listener(ClientChatMessagePacket packet, Player player) {
        String message = Chat.uncoloredLegacyText(packet.message);

        CommandManager commandManager = MinecraftServer.getCommandManager();
        String cmdPrefix = commandManager.getCommandPrefix();
        if (message.startsWith(cmdPrefix)) {
            // The message is a command
            message = message.replaceFirst(cmdPrefix, "");

            PlayerCommandEvent playerCommandEvent = new PlayerCommandEvent(player, message);
            player.callCancellableEvent(PlayerCommandEvent.class, playerCommandEvent, () -> {
                commandManager.execute(player, playerCommandEvent.getCommand());
            });

            // Do not call chat event
            return;
        }


        PlayerChatEvent playerChatEvent = new PlayerChatEvent(player, MinecraftServer.getConnectionManager().getOnlinePlayers(), message);

        // Default format
        playerChatEvent.setChatFormat((event) -> {
            String username = player.getUsername();

            TextObject usernameText = TextBuilder.of(String.format("<%s>", username))
                    .color(ChatColor.WHITE)
                    .hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatColor.GRAY + "Its " + username))
                    .clickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + username + " "))
                    .append(" " + event.getMessage())
                    .build();

            return usernameText;
        });

        // Call the event
        player.callCancellableEvent(PlayerChatEvent.class, playerChatEvent, () -> {

            Function<PlayerChatEvent, TextObject> formatFunction = playerChatEvent.getChatFormatFunction();
            if (formatFunction == null)
                throw new NullPointerException("PlayerChatEvent#chatFormat cannot be null!");

            TextObject textObject = formatFunction.apply(playerChatEvent);

            for (Player recipient : playerChatEvent.getRecipients()) {
                recipient.sendMessage(textObject);
            }

        });

    }

}
