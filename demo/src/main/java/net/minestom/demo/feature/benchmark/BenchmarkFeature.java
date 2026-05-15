package net.minestom.demo.feature.benchmark;

import net.kyori.adventure.text.Component;
import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Enables {@link BenchmarkManager} for CPU sampling and posts a tab-list
 * header/footer every 10 ticks with RAM usage, tick time, acquisition
 * time, and the CPU monitoring breakdown. Subscribes to
 * {@link ServerTickMonitorEvent} so it stays in sync with the actual
 * tick rate.
 */
public final class BenchmarkFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        BenchmarkManager benchmarkManager = process.benchmark();
        benchmarkManager.enable(Duration.of(10, TimeUnit.SECOND));

        AtomicReference<TickMonitor> lastTick = new AtomicReference<>();
        process.eventHandler().addListener(ServerTickMonitorEvent.class, event -> lastTick.set(event.getTickMonitor()));

        process.scheduler().buildTask(() -> {
            if (lastTick.get() == null || process.connection().getOnlinePlayerCount() == 0) return;

            long ramUsage = benchmarkManager.getUsedMemory() / (long) 1e6;
            TickMonitor tickMonitor = lastTick.get();
            final Component header = Component.text("RAM USAGE: " + ramUsage + " MB")
                    .append(Component.newline())
                    .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms"))
                    .append(Component.newline())
                    .append(Component.text("ACQ TIME: " + MathUtils.round(tickMonitor.getAcquisitionTime(), 2) + "ms"));
            final Component footer = benchmarkManager.getCpuMonitoringMessage();
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }
}
