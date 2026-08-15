package net.minestom.server.network.packet.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketWriting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a packet whose framed representation is computed lazily and
 * cached for reuse.
 *
 * <p>The cached value is stored in a {@link SoftReference}, allowing it to be
 * reclaimed in response to memory pressure. The value is recomputed when the
 * reference has been cleared or when {@link #invalidate()} is called.
 *
 * <p>When caching is enabled, cache computation is serialized so that only one
 * thread invokes the packet supplier and computes a replacement value at a
 * time. Cache reads and publication use acquire and release memory semantics.
 *
 * <p>When caching is disabled, calls to {@link #packet(ConnectionState)} invoke
 * the packet supplier directly. The supplier must therefore be thread safe if
 * this object may be accessed concurrently while caching is disabled.
 *
 * <p>The supplied packet and its serialized representation must remain valid
 * for the {@link ConnectionState} used to populate the cache. A
 * {@code CachedPacket} should not be shared across connection states when the
 * resulting framed representation differs between those states.
 */
@ApiStatus.Internal
public final class CachedPacket implements SendablePacket {
    private static final VarHandle PACKET;

    static {
        try {
            PACKET = MethodHandles.lookup().findVarHandle(CachedPacket.class, "packet", SoftReference.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Supplier<ServerPacket> packetSupplier;

    // Accessed through PACKET using acquire/release semantics.
    @SuppressWarnings("unused")
    private @Nullable SoftReference<FramedPacket> packet;

    /**
     * Creates a cached packet backed by the given packet supplier.
     *
     * <p>The supplier is invoked whenever the framed packet must be recomputed.
     * It may therefore be invoked multiple times over the lifetime of this
     * object.
     *
     * <p>When caching is enabled, supplier invocation is serialized. When
     * caching is disabled, the supplier may be invoked concurrently and must be
     * thread safe if this object is accessed by multiple threads.
     *
     * @param packetSupplier the packet supplier
     * @throws NullPointerException if {@code packetSupplier} is {@code null}
     */
    public CachedPacket(Supplier<ServerPacket> packetSupplier) {
        this.packetSupplier = Objects.requireNonNull(packetSupplier, "packetSupplier");
    }

    /**
     * Creates a cached packet backed by a constant packet value.
     *
     * @param packet the packet to frame and cache
     * @throws NullPointerException if {@code packet} is {@code null}
     */
    public CachedPacket(ServerPacket packet) {
        Objects.requireNonNull(packet, "packet");
        this(() -> packet);
    }

    /**
     * Invalidates the currently cached packet, if any.
     *
     * <p>The next cache access recomputes the packet unless caching is disabled.
     * This method has no effect when caching is disabled.
     *
     * @implSpec This method clears the cached reference using release semantics.
     * Cache computation is not synchronized with invalidation, so an in progress
     * computation may publish a value after this method returns.
     */
    public void invalidate() {
        if (!ServerFlag.CACHED_PACKET) return;
        PACKET.setRelease(this, null);
    }

    /**
     * Returns the packet represented by this cached value.
     *
     * <p>When caching is enabled, this method may initialize or reuse the cached
     * framed packet. Supplier invocation and cache computation are serialized.
     *
     * <p>When caching is disabled, the packet supplier is invoked directly and
     * may be invoked concurrently by multiple callers.
     *
     * @param state the connection state used when framing the packet
     * @return the packet value
     */
    public ServerPacket packet(ConnectionState state) {
        final FramedPacket cache = updatedCache(state);
        return cache != null ? cache.packet() : packetSupplier.get();
    }

    /**
     * Returns the cached framed packet body for the given connection state.
     *
     * <p>If caching is enabled, this method initializes the cache when necessary.
     * Cache computation and supplier invocation are serialized.
     *
     * <p>If caching is disabled, no framed body is created and {@code null} is
     * returned.
     *
     * @param state the connection state used when framing the packet
     * @return the framed packet body, or {@code null} when caching is disabled
     */
    public @Nullable NetworkBuffer body(ConnectionState state) {
        final FramedPacket cache = updatedCache(state);
        return cache != null ? cache.body() : null;
    }

    /**
     * Returns the current cached value, computing it when absent.
     *
     * @param state the connection state used when framing the packet
     * @return the cached framed packet, or {@code null} when caching is disabled
     */
    private @Nullable FramedPacket updatedCache(ConnectionState state) {
        if (!ServerFlag.CACHED_PACKET) return null;

        final FramedPacket cache = cachedPacket();
        return cache != null ? cache : computeCache(state);
    }

    /**
     * Returns an existing cached value or computes and publishes a replacement.
     *
     * <p>This method is synchronized so that only one thread invokes the packet
     * supplier and computes a replacement cache entry at a time. The cache is
     * checked again after acquiring this object's monitor because another thread
     * may have populated it while the current thread was waiting.
     *
     * <p>Invalidation does not acquire this monitor. An invalidation occurring
     * during computation may therefore be followed by publication of the
     * in progress result.
     *
     * @param state the connection state used when framing the packet
     * @return the existing or newly computed framed packet
     */
    private synchronized FramedPacket computeCache(ConnectionState state) {
        final FramedPacket cache = cachedPacket();
        if (cache != null) return cache;

        final ServerPacket packet = packetSupplier.get();
        final NetworkBuffer buffer = PacketWriting.allocateTrimmedPacket(state, packet, MinecraftServer.getCompressionThreshold());

        final FramedPacket updated = new FramedPacket(packet, buffer);
        PACKET.setRelease(this, new SoftReference<>(updated));
        return updated;
    }

    /**
     * Returns the currently referenced cached packet.
     *
     * <p>The cached reference is read using acquire semantics.
     *
     * @return the cached framed packet, or {@code null} if no cache exists or
     * the soft reference has been cleared
     */
    @SuppressWarnings("unchecked")
    private @Nullable FramedPacket cachedPacket() {
        final SoftReference<FramedPacket> ref = (SoftReference<FramedPacket>) PACKET.getAcquire(this);
        return ref != null ? ref.get() : null;
    }

    /**
     * Returns whether a framed packet is currently cached and has not been
     * reclaimed.
     *
     * <p>This result is inherently transient because the underlying soft
     * reference may be cleared or the cache may be invalidated immediately
     * after this method returns.
     *
     * @return {@code true} if caching is enabled and a cached value is currently
     * available
     */
    public boolean isValid() {
        return ServerFlag.CACHED_PACKET && cachedPacket() != null;
    }

    /**
     * Returns a string representation containing the currently referenced
     * cached value.
     *
     * <p>Calling this method does not invoke the packet supplier or repopulate
     * the cache.
     *
     * @return a string representation of this cached packet
     */
    @Override
    public String toString() {
        return String.format("CachedPacket{cache=%s}", cachedPacket());
    }
}