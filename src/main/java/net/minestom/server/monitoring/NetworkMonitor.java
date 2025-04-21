package net.minestom.server.monitoring;

import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkMonitor {

    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final AtomicLong incomingByteCounter = new AtomicLong();
    private final AtomicLong outgoingByteCounter = new AtomicLong();
    private final AtomicLong incomingPacketCounter = new AtomicLong();
    private final AtomicLong outgoingPacketCounter = new AtomicLong();

    private final AtomicLong lastResetTime = new AtomicLong();


    public void enable() {
        this.enabled.set(true);
        this.reset();
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void disable() {
        this.enabled.set(false);
        this.reset();
    }

    public void addIncomingBytes(long bytes) {
        incomingByteCounter.addAndGet(bytes);
        incomingPacketCounter.incrementAndGet();
    }

    public void addOutgoingBytes(long bytes) {
        outgoingByteCounter.addAndGet(bytes);
        outgoingPacketCounter.incrementAndGet();
    }

    public AtomicBoolean enabled() {
        return enabled;
    }

    public NetworkResult getResult() {
        return getResult(false);
    }

    public NetworkResult getResultAndReset() {
        return getResult(true);
    }

    @ApiStatus.Internal
    private NetworkResult getResult(boolean reset) {
        return NetworkResult.of(this, reset);
    }

    public void reset() {
        lastResetTime.set(System.currentTimeMillis());
        incomingByteCounter.set(0);
        outgoingByteCounter.set(0);
        incomingPacketCounter.set(0);
        outgoingPacketCounter.set(0);
    }

    public record NetworkResult(long incomingBytes, long outgoingBytes, long incomingPackets, long outgoingPackets,
                                long lastResetTime) {
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
