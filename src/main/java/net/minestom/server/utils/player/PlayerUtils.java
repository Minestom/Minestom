package net.minestom.server.utils.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.network.player.FakePlayerConnection;
import net.minestom.server.network.player.PlayerConnection;

public class PlayerUtils {

    public static boolean isNettyClient(Entity entity) {
        return (entity instanceof Player) && !(entity instanceof FakePlayer);
    }

    public static boolean isNettyClient(Player player) {
        return !(player instanceof FakePlayer);
    }

    public static boolean isNettyClient(PlayerConnection playerConnection) {
        return !(playerConnection instanceof FakePlayerConnection);
    }

}
