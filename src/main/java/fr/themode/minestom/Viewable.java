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
        if (getViewers().isEmpty())
            return;

        //Packet p = PacketUtils.writePacket(packet);
        getViewers().forEach(player -> player.getPlayerConnection().sendPacket(packet));
    }

    default void sendPacketsToViewers(ServerPacket... packets) {
        if (getViewers().isEmpty())
            return;

        for (ServerPacket packet : packets) {
            //Packet p = PacketUtils.writePacket(packet);
            getViewers().forEach(player -> player.getPlayerConnection().sendPacket(packet));
        }
    }

    default void sendPacketToViewersAndSelf(ServerPacket packet) {
        if (this instanceof Player) {
            //Packet p = PacketUtils.writePacket(packet);
            ((Player) this).getPlayerConnection().sendPacket(packet);
            if (!getViewers().isEmpty())
                getViewers().forEach(player -> player.getPlayerConnection().sendPacket(packet));
        }
    }

}
