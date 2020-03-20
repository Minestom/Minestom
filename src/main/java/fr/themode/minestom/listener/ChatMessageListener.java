package fr.themode.minestom.listener;

import club.thectm.minecraft.text.*;
import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.PlayerChatEvent;
import fr.themode.minestom.net.packet.client.play.ClientChatMessagePacket;

import java.util.function.Function;

public class ChatMessageListener {

    public static void listener(ClientChatMessagePacket packet, Player player) {
        String message = Chat.uncoloredLegacyText(packet.message);

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
