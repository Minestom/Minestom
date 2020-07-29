package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.chat.ChatClickEvent;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ChatHoverEvent;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.RichMessage;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

public class ChatMessageListener {

    private static final CommandManager COMMAND_MANAGER = MinecraftServer.getCommandManager();
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    public static void listener(ClientChatMessagePacket packet, Player player) {
        String message = packet.message;

        String cmdPrefix = COMMAND_MANAGER.getCommandPrefix();
        if (message.startsWith(cmdPrefix)) {
            // The message is a command
            message = message.replaceFirst(cmdPrefix, "");

            PlayerCommandEvent playerCommandEvent = new PlayerCommandEvent(player, message);
            player.callCancellableEvent(PlayerCommandEvent.class, playerCommandEvent, () -> {
                COMMAND_MANAGER.execute(player, playerCommandEvent.getCommand());
            });

            // Do not call chat event
            return;
        }


        final Collection<Player> players = CONNECTION_MANAGER.getOnlinePlayers();
        PlayerChatEvent playerChatEvent = new PlayerChatEvent(player, players, message);

        // Call the event
        player.callCancellableEvent(PlayerChatEvent.class, playerChatEvent, () -> {

            final Function<PlayerChatEvent, RichMessage> formatFunction = playerChatEvent.getChatFormatFunction();

            RichMessage textObject;

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

                PacketWriterUtils.writeAndSend(recipients, chatMessagePacket);
            }

        });

    }

    private static RichMessage buildDefaultChatMessage(PlayerChatEvent chatEvent) {
        final String username = chatEvent.getSender().getUsername();

        final ColoredText usernameText = ColoredText.of(String.format("<%s>", username));

        ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD, (byte) 1);
        itemStack.addAttribute(
                new ItemAttribute(UUID.randomUUID(), Attribute.ATTACK_DAMAGE.getKey(),
                        Attribute.ATTACK_DAMAGE, AttributeOperation.ADDITION, 16.0, AttributeSlot.MAINHAND)
        );
        itemStack.setDisplayName(ColoredText.of(ChatColor.BRIGHT_GREEN + "Weird" + ChatColor.YELLOW + "Fence"));

        final RichMessage richMessage = RichMessage.of(usernameText)
                .setHoverEvent(ChatHoverEvent.showItem(itemStack))
                .setClickEvent(ChatClickEvent.suggestCommand("/msg " + username + " "))
                .append(ColoredText.of(" " + chatEvent.getMessage()));

        return richMessage;
    }

}
