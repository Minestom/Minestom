package net.minestom.server.network.packet.server;

import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a packet which can be sent to a player using {@link PlayerConnection#sendPacket(ServerPacket)}.
 * This packet serialization depends on the players it's about to be sent to.
 */
public interface ServerPlayerSpecificPacket extends ServerPacket {

    /**
     * Writes the packet to {@link BinaryWriter} in a form for a given {@link Player}.
     *
     * @param writer the writer to write the packet to.
     * @param player the player to write the packet for.
     */
    void writeForSpecificPlayer(@NotNull BinaryWriter writer, @Nullable Player player);

    @Override
    default void write(@NotNull BinaryWriter writer) {
        throw new UnsupportedOperationException("Use writeForSpecificPlayer() instead");
    }

    @Override
    default boolean isPlayerSpecific() {
        return true;
    }

}
