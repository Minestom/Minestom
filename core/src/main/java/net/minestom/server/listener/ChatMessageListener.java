package net.minestom.server.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

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
        String finalMessage = message;
        PlayerChatEvent playerChatEvent = new PlayerChatEvent(player, players, () -> buildDefaultChatMessage(player, finalMessage), message);

        // Call the event
        player.callCancellableEvent(PlayerChatEvent.class, playerChatEvent, () -> {

            final Function<PlayerChatEvent, Component> formatFunction = playerChatEvent.getChatFormatFunction();

            Component textObject;

            if (formatFunction != null) {
                // Custom format
                textObject = formatFunction.apply(playerChatEvent);
            } else {
                // Default format
                textObject = playerChatEvent.getDefaultChatFormat().get();
            }

            final Collection<Player> recipients = playerChatEvent.getRecipients();
            if (!recipients.isEmpty()) {
                // Send the message with the correct player UUID
                ChatMessagePacket chatMessagePacket =
                        new ChatMessagePacket(textObject, ChatMessagePacket.Position.CHAT, player.getUuid());

                PacketUtils.sendGroupedPacket(recipients, chatMessagePacket);
            }

        });

    }

    private static @NotNull Component buildDefaultChatMessage(@NotNull Player player, @NotNull String message) {
        final String username = player.getUsername();
        return Component.translatable("chat.type.text")
                .args(Component.text(username)
                                .insertion(username)
                                .clickEvent(ClickEvent.suggestCommand("/msg " + username + " "))
                                .hoverEvent(player),
                        Component.text(message)
                );
    }

}