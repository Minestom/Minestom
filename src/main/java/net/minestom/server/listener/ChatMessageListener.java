package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.*;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;
import net.minestom.server.utils.PacketUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.function.Function;

public class ChatMessageListener {

    private static final CommandManager COMMAND_MANAGER = MinecraftServer.getCommandManager();
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    public static void listener(ClientChatMessagePacket packet, Player player) {
        String message = packet.message;

        final String cmdPrefix = CommandManager.COMMAND_PREFIX;
        if (message.startsWith(cmdPrefix)) {
            // The message is a command
            message = message.replaceFirst(cmdPrefix, "");

            COMMAND_MANAGER.execute(player, message);

            // Do not call chat event
            return;
        }

        final Collection<Player> players = CONNECTION_MANAGER.getOnlinePlayers();
        PlayerChatEvent playerChatEvent = new PlayerChatEvent(player, players, message);

        // Call the event
        player.callCancellableEvent(PlayerChatEvent.class, playerChatEvent, () -> {

            final Function<PlayerChatEvent, JsonMessage> formatFunction = playerChatEvent.getChatFormatFunction();

            JsonMessage textObject;

            if (formatFunction != null) {
                // Custom format
                textObject = formatFunction.apply(playerChatEvent);
            } else {
                // Default format
                textObject = buildDefaultChatMessage(playerChatEvent);
            }

            final Collection<Player> recipients = playerChatEvent.getRecipients();
            if (!recipients.isEmpty()) {
                final String jsonMessage = textObject.toString();

                // Send the message with the correct player UUID
                ChatMessagePacket chatMessagePacket =
                        new ChatMessagePacket(jsonMessage, ChatMessagePacket.Position.CHAT, player.getUuid());

                PacketUtils.sendGroupedPacket(recipients, chatMessagePacket);
            }

        });

    }

    private static RichMessage buildDefaultChatMessage(PlayerChatEvent chatEvent) {
        final String username = chatEvent.getPlayer().getUsername();

        final ColoredText usernameText = ColoredText.of(String.format("<%s>", username));

        return RichMessage.of(usernameText)
                .setHoverEvent(ChatHoverEvent.showText("Click to send a message to " + username))
                .setClickEvent(ChatClickEvent.suggestCommand("/msg " + username + StringUtils.SPACE))
                .append(ColoredText.of(StringUtils.SPACE + chatEvent.getMessage()));
    }

}
