package net.minestom.server.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.entity.Player;
import net.minestom.server.message.registry.CommonChatType;
import net.minestom.server.network.packet.server.play.PlayerChatMessagePacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

/**
 * Utility class to handle client chat settings.
 */
public final class Messenger {

    private Messenger() {
        //no instance
    }
    private static SystemChatPacket CANNOT_SEND_PACKET;

    static {
        setRejectionMessage(Component.translatable("chat.cannotSend", NamedTextColor.RED));
    }

    // TODO Should we add overrides with CommonChatType and/or ChatType (and perform protocol id lookup behind the scenes)?
    // TODO Javadoc
    public static void sendSignedMessage(@NotNull Collection<Player> recipients, @NotNull MessageSender sender,
                                         @NotNull Component message, @Nullable Component unsignedMessage,
                                         @NotNull MessageSignature signature, int chatType) {
        PacketUtils.sendGroupedPacket(recipients, new PlayerChatMessagePacket(message, unsignedMessage, chatType,
                sender.displayName(), sender.teamName(), signature),
                player -> getChatPreference(player) == ChatPreference.FULL);
    }

    public static void sendSignedMessage(@NotNull Collection<Player> recipients, @NotNull MessageSender sender,
                                         @NotNull Component message, @Nullable Component unsignedMessage,
                                         @NotNull MessageSignature signature) {
        sendSignedMessage(recipients, sender, message, unsignedMessage, signature, CommonChatType.CHAT.getId());
    }

    public static void sendSignedMessage(@NotNull Collection<Player> recipients, @NotNull MessageSender sender,
                                         @NotNull Component message, @NotNull MessageSignature signature,
                                         int chatType) {
        sendSignedMessage(recipients, sender, message, null, signature, chatType);
    }

    public static void sendSignedMessage(@NotNull Collection<Player> recipients, @NotNull MessageSender sender,
                                         @NotNull Component message, @NotNull MessageSignature signature) {
        sendSignedMessage(recipients, sender, message, null, signature, CommonChatType.CHAT.getId());
    }

    public static void sendUnsignedMessage(@NotNull Collection<Player> recipients, @NotNull MessageSender sender,
                                            @NotNull Component message, int chatType) {
        sendSignedMessage(recipients, sender, message, null, MessageSignature.UNSIGNED, chatType);
    }

    public static void sendUnsignedMessage(@NotNull Collection<Player> recipients, @NotNull MessageSender sender,
                                            @NotNull Component message) {
        sendUnsignedMessage(recipients, sender, message, CommonChatType.CHAT.getId());
    }

    public static void sendSystemMessage(@NotNull Collection<Player> recipients, @NotNull Component message, int chatType) {
        PacketUtils.sendGroupedPacket(recipients, new SystemChatPacket(message, chatType), player -> {
            final ChatPreference preference = getChatPreference(player);
            return preference == ChatPreference.FULL || preference == ChatPreference.SYSTEM;
        });
    }

    public static void sendSystemMessage(@NotNull Collection<Player> recipients, @NotNull Component message) {
        sendSystemMessage(recipients, message, CommonChatType.CHAT.getId());
    }

    /**
     * Checks if the server should receive messages from a player, given their chat settings.
     *
     * @param player the player
     * @return if the server should receive messages from them
     */
    public static boolean canReceiveMessage(@NotNull Player player) {
        return getChatPreference(player) == ChatPreference.FULL;
    }

    /**
     * Checks if the server should receive commands from a player, given their chat settings.
     *
     * @param player the player
     * @return if the server should receive commands from them
     */
    public static boolean canReceiveCommand(@NotNull Player player) {
        return getChatPreference(player) != ChatPreference.NONE;
    }

    public static void setRejectionMessage(Component rejectionMessage) {
        CANNOT_SEND_PACKET = new SystemChatPacket(rejectionMessage, CommonChatType.SYSTEM.getId());
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
     * Gets the chat message type for a player, returning {@link ChatPreference#FULL} if not set.
     *
     * @param player the player
     * @return the chat message type
     */
    private static @NotNull ChatPreference getChatPreference(@NotNull Player player) {
        return Objects.requireNonNullElse(player.getSettings().getChatPreference(), ChatPreference.FULL);
    }
}
