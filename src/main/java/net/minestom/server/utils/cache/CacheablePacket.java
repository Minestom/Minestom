package net.minestom.server.utils.cache;

import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Implemented by {@link ServerPacket server packets} which can be temporary cached in memory to be re-sent later
 * without having to go through all the writing and compression.
 * <p>
 * {@link #getIdentifier()} is to differentiate this packet from the others of the same type.
 */
public interface CacheablePacket {

    /**
     * Gets the cache linked to this packet.
     * <p>
     * WARNING: the cache needs to be shared between all the object instances, tips is to make it static.
     *
     * @return the temporary packet cache
     */
    @NotNull
    TemporaryCache<TimedBuffer> getCache();

    /**
     * Gets the identifier of this packet.
     * <p>
     * Used to verify if this packet is already cached or not.
     *
     * @return this packet identifier, null to prevent caching
     */
    @Nullable
    UUID getIdentifier();

    /**
     * Gets the last time this packet changed.
     *
     * @return the last packet update time in milliseconds
     */
    long getTimestamp();

}
