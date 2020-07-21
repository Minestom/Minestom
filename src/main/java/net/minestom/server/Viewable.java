package net.minestom.server;

import net.minestom.server.entity.Player;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.HashSet;
import java.util.Set;

/**
 * Represent something in the instance which can be showed or hidden to players
 */
public interface Viewable {

    /**
     * @param player the viewer to add
     * @return true if the player has been added, false otherwise (could be because he is already a viewer)
     */
    boolean addViewer(Player player);

    /**
     * @param player the viewer to remove
     * @return true if the player has been removed, false otherwise (could be because he was not a viewer)
     */
    boolean removeViewer(Player player);

    /**
     * Get all the viewers of this viewable element
     *
     * @return A Set containing all the element's viewers
     */
    Set<Player> getViewers();

    /**
     * Get if a player is seeing this viewable object
     *
     * @param player the player to check
     * @return true if {@code player} is a viewer, false otherwise
     */
    default boolean isViewer(Player player) {
        return getViewers().contains(player);
    }

    /**
     * Send a packet to all viewers
     * <p>
     * It is better than looping through the viewers
     * to send a packet since it is here only serialized once
     *
     * @param packet the packet to send to all viewers
     */
    default void sendPacketToViewers(ServerPacket packet) {
        PacketWriterUtils.writeAndSend(getViewers(), packet);
    }

    /**
     * Send multiple packets to all viewers
     * <p>
     * It is better than looping through the viewers
     * to send a packet since it is here only serialized once
     *
     * @param packets the packets to send
     */
    default void sendPacketsToViewers(ServerPacket... packets) {
        for (ServerPacket packet : packets) {
            PacketWriterUtils.writeAndSend(getViewers(), packet);
        }
    }

    /**
     * Send a packet to all viewers and the viewable element if it is a player
     * <p>
     * If 'this' isn't a player, then {@link #sendPacketToViewers(ServerPacket)} is called instead
     *
     * @param packet the packet to send
     */
    default void sendPacketToViewersAndSelf(ServerPacket packet) {
        if (this instanceof Player) {
            if (getViewers().isEmpty()) {
                ((Player) this).getPlayerConnection().sendPacket(packet);
            } else {
                UNSAFE_sendPacketToViewersAndSelf(packet);
            }
        } else {
            sendPacketToViewers(packet);
        }
    }

    private void UNSAFE_sendPacketToViewersAndSelf(ServerPacket packet) {
        Set<Player> recipients = new HashSet<>(getViewers());
        recipients.add((Player) this);
        PacketWriterUtils.writeAndSend(recipients, packet);
    }

}
