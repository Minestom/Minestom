package net.minestom.server.utils.cache;

import io.netty.buffer.ByteBuf;
import net.minestom.server.network.netty.packet.FramedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;
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
    @NotNull TemporaryPacketCache getCache();

    /**
     * Gets the identifier of this packet.
     * <p>
     * Used to verify if this packet is already cached or not.
     *
     * @return this packet identifier, null to prevent caching
     */
    @Nullable UUID getIdentifier();

    /**
     * Gets the last time this packet changed.
     *
     * @return the last packet update time in milliseconds
     */
    long getTimestamp();

    static @Nullable FramedPacket getCache(@NotNull ServerPacket serverPacket) {
        if (!(serverPacket instanceof CacheablePacket))
            return null;

        final CacheablePacket cacheablePacket = (CacheablePacket) serverPacket;
        final UUID identifier = cacheablePacket.getIdentifier();
        if (identifier == null) {
            // This packet explicitly asks to do not retrieve the cache
            return null;
        } else {
            final long timestamp = cacheablePacket.getTimestamp();
            // Try to retrieve the cached buffer
            TemporaryCache<TimedBuffer> temporaryCache = cacheablePacket.getCache();
            TimedBuffer timedBuffer = temporaryCache.retrieve(identifier);

            // Update the buffer if non-existent or outdated
            final boolean shouldUpdate = timedBuffer == null ||
                    timestamp > timedBuffer.getTimestamp();

            if (shouldUpdate) {
                // Buffer freed by guava cache #removalListener
                final ByteBuf buffer = PacketUtils.createFramedPacket(serverPacket);
                timedBuffer = new TimedBuffer(buffer, timestamp);
                temporaryCache.cache(identifier, timedBuffer);
            }

            return new FramedPacket(timedBuffer.getBuffer());
        }
    }

    static void writeCache(@NotNull ByteBuf buffer, @NotNull ServerPacket serverPacket) {
        FramedPacket framedPacket = CacheablePacket.getCache(serverPacket);
        if (framedPacket == null) {
            PacketUtils.writeFramedPacket(buffer, serverPacket);
            return;
        }
        final ByteBuf body = framedPacket.getBody();
        synchronized (body) {
            if (framedPacket.getBody().refCnt() != 0) {
                buffer.writeBytes(body, body.readerIndex(), body.readableBytes());
            } else {
                PacketUtils.writeFramedPacket(buffer, serverPacket);
            }
        }
    }

}
