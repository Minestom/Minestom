package net.minestom.server.network;

import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@FunctionalInterface
public interface AntiCheatProvider {
    @Nullable AntiCheat createAntiCheat(UUID uuid, PlayerConnection connection);
}
