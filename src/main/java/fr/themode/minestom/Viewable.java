package fr.themode.minestom;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.ServerPacket;

import java.util.Set;

public interface Viewable {

    void addViewer(Player player);

    void removeViewer(Player player);

    Set<Player> getViewers();

    default boolean isViewer(Player player) {
        return getViewers().contains(player);
    }

    default void sendPacketToViewers(ServerPacket packet) {
        getViewers().forEach(player -> player.getPlayerConnection().sendPacket(packet));
    }

    default void sendPacketsToViewers(ServerPacket... packets) {
        getViewers().forEach(player -> {
            for (ServerPacket packet : packets)
                player.getPlayerConnection().sendPacket(packet);
        });
    }

}
