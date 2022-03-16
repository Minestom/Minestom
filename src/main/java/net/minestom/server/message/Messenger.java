package net.minestom.server.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility class to handle client chat settings.
 */
public final class Messenger {
    /**
     * The message sent to the client if they send a chat message but it is rejected by the server.
     */
    public static final Component CANNOT_SEND_MESSAGE = Component.translatable("chat.cannotSend", NamedTextColor.RED);
    private static final UUID NO_SENDER = new UUID(0, 0);
    private static final SystemChatPacket CANNOT_SEND_PACKET = new SystemChatPacket(CANNOT_SEND_MESSAGE, ChatPosition.SYSTEM_MESSAGE.getID());

    /**
     * Sends a message to a player, respecting their chat settings.
     *
     * @param player   the player
     * @param message  the message
     * @param position the position
     * @param uuid     the UUID of the sender, if any
     * @return if the message was sent
     */
    public static boolean sendMessage(@NotNull Player player, @NotNull Component message, @NotNull ChatPosition position, @Nullable UUID uuid) {
        if (getChatMessageType(player).accepts(position)) {
            player.sendPacket(new SystemChatPacket(message, 1));
            return true;
        }
        return false;
    }

    /**
     * Sends a message to some players, respecting their chat settings.
     *
     * @param players  the players
     * @param message  the message
     * @param position the position
     * @param uuid     the UUID of the sender, if any
     */
    public static void sendMessage(@NotNull Collection<Player> players, @NotNull Component message,
                                   @NotNull ChatPosition position, @Nullable UUID uuid) {
        PacketUtils.sendGroupedPacket(players, new SystemChatPacket(message, 1),
                player -> getChatMessageType(player).accepts(position));
    }

    /**
     * Checks if the server should receive messages from a player, given their chat settings.
     *
     * @param player the player
     * @return if the server should receive messages from them
     */
    public static boolean canReceiveMessage(@NotNull Player player) {
        return getChatMessageType(player) == ChatMessageType.FULL;
    }

    /**
     * Checks if the server should receive commands from a player, given their chat settings.
     *
     * @param player the player
     * @return if the server should receive commands from them
     */
    public static boolean canReceiveCommand(@NotNull Player player) {
        return getChatMessageType(player) != ChatMessageType.NONE;
    }

    /**
     * Sends a message to the player informing them we are rejecting their message or command.
     *
     * @param player the player
     */
    public static void sendRejectionMessage(@NotNull Player player) {
        player.sendPacket(CANNOT_SEND_PACKET);
    }

    /**
     * Gets the chat message type for a player, returning {@link ChatMessageType#FULL} if not set.
     *
     * @param player the player
     * @return the chat message type
     */
    private static @NotNull ChatMessageType getChatMessageType(@NotNull Player player) {
        return Objects.requireNonNullElse(player.getSettings().getChatMessageType(), ChatMessageType.FULL);
    }
}
