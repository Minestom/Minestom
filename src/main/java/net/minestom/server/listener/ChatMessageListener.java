package net.minestom.server.listener;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerChatPreviewEvent;
import net.minestom.server.message.MessageSender;
import net.minestom.server.message.Messenger;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.client.play.ClientChatPreviewPacket;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.server.play.ChatPreviewPacket;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public class ChatMessageListener {

    private static final CommandManager COMMAND_MANAGER = MinecraftServer.getCommandManager();
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    public static void commandChatListener(ClientCommandChatPacket packet, Player player) {
        final String command = packet.message();
        if (Messenger.canReceiveCommand(player)) {
            COMMAND_MANAGER.execute(player, command, packet.argumentsSignature().signatures().size() > 0 ? packet.argumentsSignature() : null);
        } else {
            Messenger.sendRejectionMessage(player);
        }
        player.setLastPreviewedMessage(null);
    }

    public static void chatMessageListener(ClientChatMessagePacket packet, Player player) {
        final String message = packet.message();
        if (!Messenger.canReceiveMessage(player)) {
            Messenger.sendRejectionMessage(player);
            return;
        }

        final Collection<Player> players = CONNECTION_MANAGER.getOnlinePlayers();
        final Component expectedMessage = Objects.requireNonNullElseGet(player.getLastPreviewedMessage(), () -> Component.text(message));
        PlayerChatEvent event = new PlayerChatEvent(player, players, message, packet.signature().withSigner(player.getUuid()),
                MessageSender.from(player), expectedMessage);

        // Call the event
        EventDispatcher.callCancellable(event, () -> {
            player.setLastPreviewedMessage(null);
            final Collection<Player> recipients = event.getRecipients();
            if (recipients.isEmpty()) return;

            // TODO Maybe change format to BiFunction<Player, String, Component>?
            final Function<PlayerChatEvent, Component> formatFunction = event.getChatFormatFunction();

            if (formatFunction != null) {
                // Let the event modify the message
                if (event.getSignature().unsigned()) {
                    // Event handler set message unsigned -> players with
                    // "Only Show Secure Chat" option enabled won't see this message
                    Messenger.sendUnsignedMessage(event.getRecipients(), event.getSender(), formatFunction.apply(event), event.getChatType());
                } else {
                    // Send both version of message -> players will see different versions based on
                    // their "Only Show Secure Chat" option
                    Messenger.sendSignedMessage(event.getRecipients(), event.getSender(), event.getMessage(),
                            formatFunction.apply(event), event.getSignature(), event.getChatType());
                }
            } else {
                // There is no way the message got modified, send it with the original signature
                Messenger.sendSignedMessage(event.getRecipients(), event.getSender(), event.getMessage(),
                        event.getSignature(), event.getChatType());
            }
        });
    }

    public static void previewListener(ClientChatPreviewPacket packet, Player player) {
        final PlayerChatPreviewEvent event = new PlayerChatPreviewEvent(player, packet.queryId(), packet.query());
        MinecraftServer.getGlobalEventHandler().callCancellable(event,
                () -> {
                    player.sendPacket(new ChatPreviewPacket(event.getId(), event.getResult()));
                    player.setLastPreviewedMessage(event.getResult());
                });
    }
}
