package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.*;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;

import java.util.Collection;
import java.util.function.Function;

public class ChatMessageListener {

    public static void listener(ClientChatMessagePacket packet, Player player) {
        String message = packet.message;

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


        Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
        PlayerChatEvent playerChatEvent = new PlayerChatEvent(player, players, message);

        // Call the event
        player.callCancellableEvent(PlayerChatEvent.class, playerChatEvent, () -> {

            Function<PlayerChatEvent, RichMessage> formatFunction = playerChatEvent.getChatFormatFunction();

            RichMessage textObject;

            if (formatFunction != null) {
                // Custom format
                textObject = formatFunction.apply(playerChatEvent);
            } else {
                // Default format
                textObject = buildDefaultChatMessage(playerChatEvent);
            }

            Collection<Player> recipients = playerChatEvent.getRecipients();
            if (!recipients.isEmpty()) {
                String jsonMessage = textObject.toString();

                // Send the message with the correct player UUID
                ChatMessagePacket chatMessagePacket =
                        new ChatMessagePacket(jsonMessage, ChatMessagePacket.Position.CHAT, player.getUuid());

                PacketWriterUtils.writeAndSend(recipients, chatMessagePacket);
            }

        });

    }

    private static RichMessage buildDefaultChatMessage(PlayerChatEvent chatEvent) {
        String username = chatEvent.getSender().getUsername();

        ColoredText usernameText = ColoredText.of(String.format("<%s>", username));

        RichMessage richMessage = RichMessage.of(usernameText)
                .setHoverEvent(ChatHoverEvent.showText(ColoredText.of(ChatColor.GRAY + "Its " + username)))
                .setClickEvent(ChatClickEvent.suggestCommand("/msg " + username + " "))
                .append(ColoredText.of(" " + chatEvent.getMessage()));

        return richMessage;
    }

}
