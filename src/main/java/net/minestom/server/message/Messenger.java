package net.minestom.server.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;
import net.minestom.server.utils.Action;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class to handle client chat settings.
 */
public class Messenger {
    /**
     * The message sent to the client if they send a chat message but it is rejected by the server.
     */
    public static final Component CANNOT_SEND_MESSAGE = Component.translatable("chat.cannotSend", NamedTextColor.RED);

    private static final ChatMessagePacket CANNOT_SEND_PACKET = new ChatMessagePacket(CANNOT_SEND_MESSAGE, ChatPosition.SYSTEM_MESSAGE, null);

    /**
     * Sends a message to a player, respecting their chat settings.
     *
     * @param player the player
     * @param message the message
     * @param position the position
     * @param uuid the UUID of the sender, if any
     * @return if the message was sent
     */
    public static boolean sendMessage(@NotNull Player player, @NotNull Component message, @NotNull ChatPosition position, @Nullable UUID uuid) {
        if (player.getSettings().getChatMessageType().accepts(position)) {
            player.getPlayerConnection().sendPacket(new ChatMessagePacket(message, position, uuid));
            return true;
        }

        return false;
    }

    /**
     * Sends a message to some players, respecting their chat settings.
     *
     * @param players the players
     * @param message the message
     * @param position the position
     * @param uuid the UUID of the sender, if any
     * @return a set of players who received the message
     */
    public static @NotNull Set<Player> sendMessage(@NotNull Iterable<? extends Player> players, @NotNull Component message,
                                            @NotNull ChatPosition position, @Nullable UUID uuid) {
        final Set<Player> sentTo = new HashSet<>();

        for (Player player : players) {
            if (player.getSettings().getChatMessageType().accepts(position)) {
                sentTo.add(player);
            }
        }

        PacketUtils.sendGroupedPacket(sentTo, new ChatMessagePacket(message, position, uuid));
        return sentTo;
    }

    /**
     * Checks if the server should receive messages from a player, given their chat settings.
     *
     * @param player the player
     * @return if the server should receive messages from them
     */
    public static boolean canReceiveMessage(@NotNull Player player) {
        return player.getSettings().getChatMessageType() == ChatMessageType.FULL;
    }

    /**
     * Performs an action if the server can receive messages from a player.
     * This method will send the rejection message automatically.
     *
     * @param player the player
     * @param action the action
     */
    public static void receiveMessage(@NotNull Player player, @NotNull Action action) {
        if (canReceiveMessage(player)) {
            action.act();
        } else {
            sendRejectionMessage(player);
        }
    }

    /**
     * Checks if the server should receive commands from a player, given their chat settings.
     *
     * @param player the player
     * @return if the server should receive commands from them
     */
    public static boolean canReceiveCommand(@NotNull Player player) {
        return player.getSettings().getChatMessageType() != ChatMessageType.NONE;
    }

    /**
     * Performs an action if the server can receive commands from a player.
     * This method will send the rejection message automatically.
     *
     * @param player the player
     * @param action the action
     */
    public static void receiveCommand(@NotNull Player player, @NotNull Action action) {
        if (canReceiveCommand(player)) {
            action.act();
        } else {
            sendRejectionMessage(player);
        }
    }

    /**
     * Sends a message to the player informing them we are rejecting their message or command.
     *
     * @param player the player
     */
    public static void sendRejectionMessage(@NotNull Player player) {
        player.getPlayerConnection().sendPacket(CANNOT_SEND_PACKET);
    }
}
