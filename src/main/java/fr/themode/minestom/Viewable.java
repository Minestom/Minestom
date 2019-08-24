package fr.themode.minestom;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.PacketWriter;
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

        PacketWriter.writeCallbackPacket(packet, buffer -> {
            int size = getViewers().size();
            if (size == 0)
                return;
            buffer.getData().retain(getViewers().size()).markReaderIndex();
            getViewers().forEach(player -> {
                player.getPlayerConnection().writeUnencodedPacket(buffer);
                buffer.getData().resetReaderIndex();
            });
        });
    }

    default void sendPacketsToViewers(ServerPacket... packets) {
        if (getViewers().isEmpty())
            return;

        for (ServerPacket packet : packets) {
            PacketWriter.writeCallbackPacket(packet, buffer -> {
                buffer.getData().retain(getViewers().size()).markReaderIndex();
                getViewers().forEach(player -> {
                    player.getPlayerConnection().writeUnencodedPacket(buffer);
                    buffer.getData().resetReaderIndex();
                });
            });
        }
    }

    default void sendPacketToViewersAndSelf(ServerPacket packet) {
        if (this instanceof Player) {
            PacketWriter.writeCallbackPacket(packet, buffer -> {
                buffer.getData().retain(getViewers().size() + 1).markReaderIndex();
                ((Player) this).getPlayerConnection().writeUnencodedPacket(buffer);
                buffer.getData().resetReaderIndex();
                if (!getViewers().isEmpty()) {
                    getViewers().forEach(player -> {
                        buffer.getData().resetReaderIndex();
                        player.getPlayerConnection().writeUnencodedPacket(buffer);
                    });
                }
            });
        }
    }

}
