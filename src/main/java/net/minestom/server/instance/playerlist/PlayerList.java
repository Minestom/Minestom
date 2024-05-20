package net.minestom.server.instance.playerlist;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@ApiStatus.Experimental
public interface PlayerList {
    Collection<Player> getBroadcastRecipients();

    default void broadcast(@NotNull ServerPacket packet) {
        getBroadcastRecipients().forEach(player -> player.sendPacket(packet));
    }

    void send(@NotNull ServerPacket packet);

    default PlayerInfoUpdatePacket createAddPlayerToList(Player player) {
        return new PlayerInfoUpdatePacket(EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED),
                List.of(toEntry(player)));
    }

    default PlayerInfoUpdatePacket createUpdateDisplayName(Player player) {
        return new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, toEntry(player));
    }

    default PlayerInfoUpdatePacket createUpdateGameMode(Player player) {
        return new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, toEntry(player));
    }

    default PlayerInfoUpdatePacket createUpdateLatency(Player player) {
        return new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LATENCY, toEntry(player));
    }

    default PlayerInfoRemovePacket createRemovePlayer(Player player) {
        return new PlayerInfoRemovePacket(player.getUuid());
    }

    default PlayerInfoUpdatePacket.Entry toEntry(Player player) {
        final PlayerSkin skin = player.getSkin();
        List<PlayerInfoUpdatePacket.Property> prop = skin != null ?
                List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature())) :
                List.of();
        return new PlayerInfoUpdatePacket.Entry(player.getUuid(), player.getUsername(), prop,
                true, player.getLatency(), player.getGameMode(), player.getDisplayName(), null);
    }
}
