package net.minestom.jmh.ui;

import net.kyori.adventure.text.Component;
import net.minestom.server.ui.PlayerUI;
import net.minestom.server.ui.SidebarUI;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class SideBarUIBenchmark {

    PlayerUI ui;
    int count;

    @Setup
    public void setup() {
        this.ui = PlayerUI.newPlayerUI();
    }

    @Benchmark
    public void constant() {
        ui.sidebar(SidebarUI.of(Component.text("test"), List.of()));
        ui.drain(serverPacket -> {
            // Empty
        });
    }

    @Benchmark
    public void random() {
        ui.sidebar(SidebarUI.of(Component.text("test"), List.of(Component.text("count: " + (++count)))));
        ui.drain(serverPacket -> {
            // Empty
        });
    }
}
