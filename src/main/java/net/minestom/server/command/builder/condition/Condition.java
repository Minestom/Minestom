package net.minestom.server.command.builder.condition;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * Common interface for {@link ExecuteCondition} and {@link RemoveCondition}
 */
@ApiStatus.NonExtendable
public interface Condition {
    default Collection<Player> getPlayers() {
        return MinecraftServer.getConnectionManager().getOnlinePlayers();
    }
}
