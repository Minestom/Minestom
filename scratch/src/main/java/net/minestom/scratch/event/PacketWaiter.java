package net.minestom.scratch.event;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class PacketWaiter {
    private final Map<Class<?>, List<Consumer<?>>> consumers = new HashMap<>();

    public void consume(ServerPacket.Play packet) {
        consume(packet.getClass(), packet);
    }

    public void consume(ClientPacket packet) {
        consume(packet.getClass(), packet);
    }

    private void consume(Class<?> packetClass, Object packet) {
        final var type = packet.getClass();
        final var list = consumers.remove(type);
        if (list == null) return;
        for (var consumer : list) {
            ((Consumer<Object>) consumer).accept(packet);
        }
    }

    public <T extends ServerPacket.Play> void onSent(Class<T> packetClass, Consumer<T> consumer) {
        final var type = packetClass;
        consumers.computeIfAbsent(type, (key) -> new ArrayList<>()).add(consumer);
    }

    public <T extends ClientPacket> void onReceived(Class<T> packetClass, Consumer<T> consumer) {
        final var type = packetClass;
        consumers.computeIfAbsent(type, (key) -> new ArrayList<>()).add(consumer);
    }
}
