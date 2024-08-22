package net.minestom.server.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public final class PacketSendingUtils {
    /**
     * Sends a packet to an audience. This method performs the following steps in the
     * following order:
     * <ol>
     *     <li>If {@code audience} is a {@link Player}, send the packet to them.</li>
     *     <li>Otherwise, if {@code audience} is a {@link PacketGroupingAudience}, call
     *     {@link #sendGroupedPacket(Collection, ServerPacket)} on the players that the
     *     grouping audience contains.</li>
     *     <li>Otherwise, if {@code audience} is a {@link ForwardingAudience.Single},
     *     call this method on the single audience inside the forwarding audience.</li>
     *     <li>Otherwise, if {@code audience} is a {@link ForwardingAudience}, call this
     *     method for each audience member of the forwarding audience.</li>
     *     <li>Otherwise, do nothing.</li>
     * </ol>
     *
     * @param audience the audience
     * @param packet   the packet
     */
    @SuppressWarnings("OverrideOnly") // we need to access the audiences inside ForwardingAudience
    public static void sendPacket(@NotNull Audience audience, @NotNull ServerPacket packet) {
        switch (audience) {
            case Player player -> player.sendPacket(packet);
            case PacketGroupingAudience groupingAudience -> sendGroupedPacket(groupingAudience.getPlayers(), packet);
            case ForwardingAudience.Single singleAudience -> sendPacket(singleAudience.audience(), packet);
            case ForwardingAudience forwardingAudience -> {
                for (Audience member : forwardingAudience.audiences()) {
                    sendPacket(member, packet);
                }
            }
            default -> {
            }
        }
    }

    /**
     * Sends a {@link ServerPacket} to multiple players.
     * <p>
     * Can drastically improve performance since the packet will not have to be processed as much.
     *
     * @param players   the players to send the packet to
     * @param packet    the packet to send to the players
     * @param predicate predicate to ignore specific players
     */
    public static void sendGroupedPacket(@NotNull Collection<Player> players, @NotNull ServerPacket packet,
                                         @NotNull Predicate<Player> predicate) {
        final SendablePacket sendablePacket = groupedPacket(packet);
        players.forEach(player -> {
            if (predicate.test(player)) player.sendPacket(sendablePacket);
        });
    }

    /**
     * Same as {@link #sendGroupedPacket(Collection, ServerPacket, Predicate)}
     * but without any predicate.
     *
     * @see #sendGroupedPacket(Collection, ServerPacket, Predicate)
     */
    public static void sendGroupedPacket(@NotNull Collection<Player> players, @NotNull ServerPacket packet) {
        final SendablePacket sendablePacket = groupedPacket(packet);
        players.forEach(player -> player.sendPacket(sendablePacket));
    }

    public static void broadcastPlayPacket(@NotNull ServerPacket packet) {
        sendGroupedPacket(MinecraftServer.getConnectionManager().getOnlinePlayers(), packet);
    }

    static SendablePacket groupedPacket(ServerPacket packet) {
        return shouldUseCachePacket(packet) ? new CachedPacket(packet) : packet;
    }

    /**
     * Checks if the {@link ServerPacket} is suitable to be wrapped into a {@link CachedPacket}.
     * Note: {@link ServerPacket.ComponentHolding}s are not translated inside a {@link CachedPacket}.
     *
     * @see CachedPacket#body(ConnectionState)
     */
    static boolean shouldUseCachePacket(final @NotNull ServerPacket packet) {
        if (!MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION) return ServerFlag.GROUPED_PACKET;
        if (!(packet instanceof ServerPacket.ComponentHolding holder)) return ServerFlag.GROUPED_PACKET;
        return !containsTranslatableComponents(holder);
    }

    private static boolean containsTranslatableComponents(final @NotNull ComponentHolder<?> holder) {
        for (final Component component : holder.components()) {
            if (isTranslatable(component)) return true;
        }
        return false;
    }

    private static boolean isTranslatable(final @NotNull Component component) {
        if (component instanceof TranslatableComponent) return true;
        final List<Component> children = component.children();
        if (children.isEmpty()) return false;
        for (final Component child : children) {
            if (isTranslatable(child)) return true;
        }
        return false;
    }
}
