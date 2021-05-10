package net.minestom.server.utils.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;

public final class PlayerUtils {

    private PlayerUtils() {

    }

    public static boolean isNettyClient(PlayerConnection playerConnection) {
        return playerConnection instanceof NettyPlayerConnection;
    }

    public static boolean isNettyClient(Player player) {
        return isNettyClient(player.getPlayerConnection());
    }

    public static boolean isNettyClient(Entity entity) {
        return (entity instanceof Player) && isNettyClient((Player) entity);
    }

}
