package net.minestom.server.listener.common;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerConfigCustomClickEvent;
import net.minestom.server.event.player.PlayerCustomClickEvent;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.common.ClientCustomClickActionPacket;

public final class CustomClickListener {

    public static void listener(ClientCustomClickActionPacket listener, Player player) {
        var event = player.getPlayerConnection().getConnectionState() == ConnectionState.PLAY
                ? new PlayerCustomClickEvent(player, listener.key(), listener.payload().type() == BinaryTagTypes.END ? null : listener.payload())
                : new PlayerConfigCustomClickEvent(player, listener.key(), listener.payload().type() == BinaryTagTypes.END ? null : listener.payload());
        EventDispatcher.call(event);
    }

}
