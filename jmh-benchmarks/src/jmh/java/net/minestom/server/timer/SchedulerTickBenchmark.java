package net.minestom.server.timer;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class SchedulerTickBenchmark {

    @Param({"0", "1", "5"})
    public int tickTasks;

    Scheduler scheduler;

    @Setup
    public void setup() {
        this.scheduler = Scheduler.newScheduler();
        for (int i = 0; i < this.tickTasks; i++) {
            this.scheduler.scheduleTask(() -> {
            }, TaskSchedule.nextTick(), TaskSchedule.nextTick());
        }
    }

    @Benchmark
    public void call() {
        this.scheduler.processTick();
    }
}
