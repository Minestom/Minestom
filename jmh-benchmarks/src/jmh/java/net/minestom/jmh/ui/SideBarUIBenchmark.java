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
    SidebarUI sideBar;

    @Setup
    public void setup() {
        this.ui = PlayerUI.newPlayerUI();
        this.sideBar = SidebarUI.of(Component.text("test"), List.of());
    }

    @Benchmark
    public void constant() {
        ui.sidebar(sideBar);
        ui.drain(serverPacket -> {
        });
    }

    @Benchmark
    public void constantNew() {
        ui.sidebar(SidebarUI.of(Component.text("test"), List.of()));
        ui.drain(serverPacket -> {
        });
    }

    @Benchmark
    public void randomNew() {
        ui.sidebar(SidebarUI.of(Component.text("test"), List.of(Component.text("count: " + (++count)))));
        ui.drain(serverPacket -> {
        });
    }
}
