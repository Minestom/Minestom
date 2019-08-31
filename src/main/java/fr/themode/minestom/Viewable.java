package fr.themode.minestom;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.PacketWriterUtils;
import fr.themode.minestom.net.packet.server.ServerPacket;

import java.util.HashSet;
import java.util.Set;

public interface Viewable {

    void addViewer(Player player);

    void removeViewer(Player player);

    Set<Player> getViewers();

    default boolean isViewer(Player player) {
        return getViewers().contains(player);
    }

    default void sendPacketToViewers(ServerPacket packet) {
        PacketWriterUtils.writeAndSend(getViewers(), packet);
    }

    default void sendPacketsToViewers(ServerPacket... packets) {
        for (ServerPacket packet : packets) {
            PacketWriterUtils.writeAndSend(getViewers(), packet);
        }
    }

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
