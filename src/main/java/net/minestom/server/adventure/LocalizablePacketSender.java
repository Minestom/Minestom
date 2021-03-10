package net.minestom.server.adventure;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.network.packet.server.play.TitlePacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Utility class for sending packets with translatable components. All functions in this
 * class will send grouped packets if the components do not contain any translatable
 * components. In the case that they do, the components are translated and send individually.
 */
public class LocalizablePacketSender {

    /**
     * Sends a title to many players, sending it as a grouped packet if it does not
     * contain translatable elements.
     *
     * @param players the players
     * @param title the title
     */
    public static void sendGroupedTitle(@NotNull Collection<Player> players, @NotNull Title title) {
        Component preparedTitle = MinecraftServer.getSerializationManager().prepare(title.title(), MinecraftServer.getSerializationManager().getDefaultLocale()),
                preparedSubtitle = MinecraftServer.getSerializationManager().prepare(title.subtitle(), MinecraftServer.getSerializationManager().getDefaultLocale());
        Collection<TitlePacket> rootPacket = TitlePacket.of(Title.title(preparedTitle, preparedSubtitle, title.times()));

        if (title.title().equals(preparedTitle) && title.subtitle().equals(preparedSubtitle)) {
            for (TitlePacket packet : rootPacket) {
                PacketUtils.sendGroupedPacket(players, packet);
            }
        } else {
            for (Player player : players) {
                Collection<TitlePacket> packets;

                if (player.getLocale() == null) {
                    packets = rootPacket;
                } else {
                    packets = TitlePacket.of(Title.title(MinecraftServer.getSerializationManager().prepare(title.title(), player),
                            MinecraftServer.getSerializationManager().prepare(title.subtitle(), player), title.times()));
                }

                for (TitlePacket packet : packets) {
                    player.getPlayerConnection().sendPacket(packet);
                }
            }
        }
    }

    /**
     * Sends an action bar to many players, sending it as a grouped packet if it does not
     * contain translatable elements.
     *
     * @param players the players
     * @param component the component
     */
    public static void sendGroupedActionBar(@NotNull Collection<Player> players, @NotNull Component component) {
        Component preparedComponent = MinecraftServer.getSerializationManager().prepare(component, MinecraftServer.getSerializationManager().getDefaultLocale());
        TitlePacket rootPacket = new TitlePacket(TitlePacket.Action.SET_ACTION_BAR, preparedComponent);

        if (component.equals(preparedComponent)) {
            PacketUtils.sendGroupedPacket(players, rootPacket);
        } else {
            for (Player player : players) {
                TitlePacket packet;

                if (player.getLocale() == null) {
                    packet = rootPacket;
                } else {
                    packet = new TitlePacket(TitlePacket.Action.SET_ACTION_BAR, MinecraftServer.getSerializationManager().prepare(component, player));
                }

                player.getPlayerConnection().sendPacket(packet);
            }
        }
    }

    /**
     * Sends a player list to many players, sending it as a grouped packet if it does not
     * contain translatable elements.
     *
     * @param players the players
     * @param header the header
     * @param footer  the footer
     */
    public static void sendGroupedPlayerList(@NotNull Collection<Player> players, @Nullable Component header, @Nullable Component footer) {
        // empty check first
        if (header == null) {
            header = Component.empty();
        }

        if (footer == null) {
            footer = Component.empty();
        }

        // now back to the packets
        Component preparedHeader = MinecraftServer.getSerializationManager().prepare(header, MinecraftServer.getSerializationManager().getDefaultLocale()),
                preparedFooter = MinecraftServer.getSerializationManager().prepare(footer, MinecraftServer.getSerializationManager().getDefaultLocale());
        PlayerListHeaderAndFooterPacket rootPacket = new PlayerListHeaderAndFooterPacket(preparedHeader, preparedFooter);

        if (header.equals(preparedHeader) && footer.equals(preparedFooter)) {
            PacketUtils.sendGroupedPacket(players, rootPacket);
        } else {
            for (Player player : players) {
                PlayerListHeaderAndFooterPacket packet;

                if (player.getLocale() == null) {
                    packet = rootPacket;
                } else {
                    packet = new PlayerListHeaderAndFooterPacket(MinecraftServer.getSerializationManager().prepare(header, player),
                            MinecraftServer.getSerializationManager().prepare(footer, player));
                }

                player.getPlayerConnection().sendPacket(packet);
            }
        }
    }

    /**
     * Sends a message to many players, sending it as a grouped packet if it does not
     * contain translatable elements.
     *
     * @param players the players
     * @param source the source of the message
     * @param message the message
     * @param messageType the type of the message
     */
    public static void sendGroupedMessage(@NotNull Collection<Player> players, @NotNull Identity source, @NotNull Component message, @NotNull MessageType messageType) {
        ChatMessagePacket.Position position = ChatMessagePacket.Position.fromMessageType(messageType);
        Component preparedMessage = MinecraftServer.getSerializationManager().prepare(message, MinecraftServer.getSerializationManager().getDefaultLocale());
        ChatMessagePacket rootPacket = new ChatMessagePacket(preparedMessage, position, source.uuid());

        if (message.equals(preparedMessage)) {
            PacketUtils.sendGroupedPacket(players, rootPacket);
        } else {
            for (Player player : players) {
                ChatMessagePacket packet;

                if (player.getLocale() == null) {
                    packet = rootPacket;
                } else {
                    packet = new ChatMessagePacket(MinecraftServer.getSerializationManager().prepare(message, player), position, source.uuid());
                }

                player.getPlayerConnection().sendPacket(packet);
            }
        }
    }
}
