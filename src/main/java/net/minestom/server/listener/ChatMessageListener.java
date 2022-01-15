package net.minestom.server.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.StringReader;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.message.Messenger;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Function;

public class ChatMessageListener {

    private static final CommandManager COMMAND_MANAGER = MinecraftServer.getCommandManager();
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    public static void listener(ClientChatMessagePacket packet, Player player) {
        String message = packet.message;

        if (message.startsWith(CommandManager.COMMAND_PREFIX)) {
            // Create a new reader with the message and skip the prefix because it's not part of the command
            StringReader reader = new StringReader(message, CommandManager.COMMAND_PREFIX.length());

            // check if we can receive commands
            if (Messenger.canReceiveCommand(player)) {
                COMMAND_MANAGER.execute(CommandOrigin.ofPlayer(player), reader);
            } else {
                Messenger.sendRejectionMessage(player);
            }

            // Do not call chat event
            return;
        }

        // check if we can receive messages
        if (!Messenger.canReceiveMessage(player)) {
            Messenger.sendRejectionMessage(player);
            return;
        }

        final Collection<Player> players = CONNECTION_MANAGER.getOnlinePlayers();
        PlayerChatEvent playerChatEvent = new PlayerChatEvent(player, players, () -> buildDefaultChatMessage(player, message), message);

        // Call the event
        EventDispatcher.callCancellable(playerChatEvent, () -> {
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
                // delegate to the messenger to avoid sending messages we shouldn't be
                Messenger.sendMessage(recipients, textObject, ChatPosition.CHAT, player.getUuid());
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