package net.minestom.server.monitoring;

import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * NetworkMonitor is a utility class that tracks network statistics such as
 * incoming and outgoing bytes and packets.
 * It provides methods to enable/disable monitoring,
 * add incoming/outgoing bytes,
 * get results, and reset the counters.
 */
public class NetworkMonitor {

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    private final AtomicLong incomingByteCounter = new AtomicLong();
    private final AtomicLong outgoingByteCounter = new AtomicLong();
    private final AtomicLong incomingPacketCounter = new AtomicLong();
    private final AtomicLong outgoingPacketCounter = new AtomicLong();

    private final AtomicLong lastResetTime = new AtomicLong();

    /**
     * Enables the NetworkMonitor.
     */
    public void enable() {
        this.enabled.set(true);
        this.reset();
    }

    /**
     * Checks if the NetworkMonitor is enabled.
     *
     * @return true if enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled.get();
    }

    /**
     * Disables the NetworkMonitor.
     * This will also reset the counters.
     */
    public void disable() {
        this.enabled.set(false);
        this.reset();
    }

    /**
     * Adds incoming {@code bytes} to the counter.
     * This method is thread-safe.
     * This will also increment the {@code incomingByteCounter}.
     *
     * @param bytes the number of bytes to add
     */
    public void addIncomingBytes(long bytes) {
        incomingByteCounter.addAndGet(bytes);
        incomingPacketCounter.incrementAndGet();
    }

    /**
     * Adds outgoing {@code bytes} to the counter.
     * This method is thread-safe.
     * This will also increment the {@code outgoingPacketCounter}.
     *
     * @param bytes the number of bytes to add
     */
    public void addOutgoingBytes(long bytes) {
        outgoingByteCounter.addAndGet(bytes);
        outgoingPacketCounter.incrementAndGet();
    }

    /**
     * Gets the current statistics of the NetworkMonitor.
     *
     * @return the NetworkResult object with the current statistics
     */
    public NetworkResult getResult() {
        return getResult(false);
    }

    /**
     * Gets the current statistics of the NetworkMonitor and resets the counters.
     * This method is thread-safe.
     *
     * @return the NetworkResult object with the current statistics
     */
    public NetworkResult getResultAndReset() {
        return getResult(true);
    }

    /**
     * Gets the current statistics of the NetworkMonitor.
     *
     * @param reset true to reset the counters, false otherwise
     * @return the NetworkResult object with the current statistics
     */
    @ApiStatus.Internal
    private NetworkResult getResult(boolean reset) {
        return NetworkResult.of(this, reset);
    }

    /**
     * Resets the NetworkMonitor counters.
     * This method is thread-safe.
     */
    public void reset() {
        lastResetTime.set(System.currentTimeMillis());
        incomingByteCounter.set(0);
        outgoingByteCounter.set(0);
        incomingPacketCounter.set(0);
        outgoingPacketCounter.set(0);
    }

    /**
     * NetworkResult is a record that holds the network statistics.
     *
     * @param incomingBytes   number of incoming bytes
     * @param outgoingBytes   number of outgoing bytes
     * @param incomingPackets number of incoming packets
     * @param outgoingPackets number of outgoing packets
     * @param lastResetTime   the last reset time in milliseconds since epoch
     */
    public record NetworkResult(long incomingBytes, long outgoingBytes, long incomingPackets, long outgoingPackets,
                                long lastResetTime) {
        /**
         * Creates a new NetworkResult object.
         *
         * @param monitor the NetworkMonitor instance
         * @param reset   true to reset the counters, false otherwise
         * @return a new NetworkResult object
         */
        static NetworkResult of(NetworkMonitor monitor, boolean reset) {
            return (
                    reset ?
                            new NetworkResult(monitor.incomingByteCounter.getAndSet(0), monitor.outgoingByteCounter.getAndSet(0), monitor.incomingPacketCounter.getAndSet(0), monitor.outgoingPacketCounter.getAndSet(0), monitor.lastResetTime.getAndSet(System.currentTimeMillis()))
                            :
                            new NetworkResult(monitor.incomingByteCounter.get(), monitor.outgoingByteCounter.get(), monitor.incomingPacketCounter.get(), monitor.outgoingPacketCounter.get(), monitor.lastResetTime.get())
            );
        }
    }
}
