package net.minestom.server.event.server;

import net.minestom.server.event.Event;
import net.minestom.server.monitoring.TickMonitor;

public value class ServerTickMonitorEvent implements Event {
    private final TickMonitor tickMonitor;

    public ServerTickMonitorEvent(TickMonitor tickMonitor) {
        this.tickMonitor = tickMonitor;
    }

    public TickMonitor getTickMonitor() {
        return tickMonitor;
    }
}
