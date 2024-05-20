package net.minestom.server.instance.playerlist;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ServerWidePlayerList implements PlayerList {
    private final Player player;

    public ServerWidePlayerList(Player player) {
        this.player = player;
    }

    @Override
    public Collection<Player> getBroadcastRecipients() {
        return MinecraftServer.getConnectionManager().getOnlinePlayers();
    }

    @Override
    public void broadcast(@NotNull ServerPacket packet) {
        PacketUtils.broadcastPlayPacket(packet);
    }

    @Override
    public void send(@NotNull ServerPacket packet) {
        player.sendPacket(packet);
    }
}
