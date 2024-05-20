package net.minestom.server.instance.playerlist;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class NoopPlayerList implements PlayerList {
    @Override
    public Collection<Player> getBroadcastRecipients() {
        return List.of();
    }

    @Override
    public void send(@NotNull ServerPacket packet) {
        // empty
    }
}
