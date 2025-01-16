package net.minestom.server.event.server;

import net.minestom.server.event.Event;
import net.minestom.server.monitoring.TickMonitor;
import org.jetbrains.annotations.NotNull;

public record ServerTickMonitorEvent(@NotNull TickMonitor tickMonitor) implements Event {}
