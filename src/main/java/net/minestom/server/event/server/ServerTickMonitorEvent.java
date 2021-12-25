package net.minestom.server.event.server;

import net.minestom.server.event.Event;
import net.minestom.server.monitoring.TickMonitor;
import org.jetbrains.annotations.NotNull;

public final class ServerTickMonitorEvent implements Event {
    private final TickMonitor tickMonitor;

    public ServerTickMonitorEvent(@NotNull TickMonitor tickMonitor) {
        this.tickMonitor = tickMonitor;
    }

    public @NotNull TickMonitor getTickMonitor() {
        return tickMonitor;
    }
}
