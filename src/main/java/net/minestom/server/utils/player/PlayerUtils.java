package net.minestom.server.utils.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.network.player.PlayerConnection;

public final class PlayerUtils {

    private PlayerUtils() {
    }

    public static boolean isSocketClient(PlayerConnection playerConnection) {
        return playerConnection instanceof PlayerSocketConnection;
    }

    public static boolean isSocketClient(Player player) {
        return isSocketClient(player.getPlayerConnection());
    }

    public static boolean isSocketClient(Entity entity) {
        return (entity instanceof Player) && isSocketClient((Player) entity);
    }
}
