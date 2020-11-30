package net.minestom.server;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents something which can be displayed or hidden to players.
 */
public interface Viewable {

    /**
     * Adds a viewer.
     *
     * @param player the viewer to add
     * @return true if the player has been added, false otherwise (could be because he is already a viewer)
     */
    boolean addViewer(@NotNull Player player);

    /**
     * Removes a viewer.
     *
     * @param player the viewer to remove
     * @return true if the player has been removed, false otherwise (could be because he was not a viewer)
     */
    boolean removeViewer(@NotNull Player player);

    /**
     * Gets all the viewers of this viewable element.
     *
     * @return A Set containing all the element's viewers
     */
    @NotNull
    Set<Player> getViewers();

    /**
     * Gets if a player is seeing this viewable object.
     *
     * @param player the player to check
     * @return true if {@code player} is a viewer, false otherwise
     */
    default boolean isViewer(@NotNull Player player) {
        return getViewers().contains(player);
    }

    /**
     * Sends a packet to all viewers.
     * <p>
     * It is better than looping through the viewers
     * to send a packet since it is here only serialized once.
     *
     * @param packet the packet to send to all viewers
     */
    default void sendPacketToViewers(@NotNull ServerPacket packet) {
        PacketUtils.sendGroupedPacket(getViewers(), packet);
    }

    /**
     * Sends multiple packets to all viewers.
     * <p>
     * It is better than looping through the viewers
     * to send a packet since it is here only serialized once.
     *
     * @param packets the packets to send
     */
    default void sendPacketsToViewers(@NotNull ServerPacket... packets) {
        for (ServerPacket packet : packets) {
            PacketUtils.sendGroupedPacket(getViewers(), packet);
        }
    }

    /**
     * Sends a packet to all viewers and the viewable element if it is a player.
     * <p>
     * If 'this' isn't a player, then {only @link #sendPacketToViewers(ServerPacket)} is called.
     *
     * @param packet the packet to send
     */
    default void sendPacketToViewersAndSelf(@NotNull ServerPacket packet) {
        if (this instanceof Player) {
            ((Player) this).getPlayerConnection().sendPacket(packet);
        }
        sendPacketToViewers(packet);
    }
    
    /**
     * Sends a packet to the viewable element if it is a player.
     * <p>
     * If 'this' isn't a player, then nothing is called.
     *
     * @param packet the packet to send
     */
    default void sendPacketToSelf(@NotNull ServerPacket packet) {
        if (this instanceof Player) {
            ((Player) this).getPlayerConnection().sendPacket(packet);
        }
    }
}
