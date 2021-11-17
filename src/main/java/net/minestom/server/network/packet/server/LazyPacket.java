package net.minestom.server.network.packet.server;

import net.minestom.server.utils.PacketUtils;

import java.util.concurrent.atomic.AtomicReference;

public final class LazyPacket implements SendablePacket {
    private final ServerPacket packet;
    private final AtomicReference<Entry> entry = new AtomicReference<>();

    public LazyPacket(ServerPacket packet) {
        this.packet = packet;
    }

    public FramedPacket retrieve() {
        Thread currentThread = Thread.currentThread();
        Entry test = entry.updateAndGet(entry ->
                entry != null && entry.thread == currentThread ? entry : new Entry(currentThread));
        return test.body;
    }

    private final class Entry {
        final Thread thread;
        FramedPacket body = PacketUtils.allocateTrimmedPacket(packet);

        public Entry(Thread thread) {
            this.thread = thread;
        }
    }
}
