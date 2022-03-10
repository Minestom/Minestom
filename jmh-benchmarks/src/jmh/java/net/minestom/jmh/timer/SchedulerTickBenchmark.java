package net.minestom.jmh.timer;

import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import org.openjdk.jmh.annotations.*;

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
