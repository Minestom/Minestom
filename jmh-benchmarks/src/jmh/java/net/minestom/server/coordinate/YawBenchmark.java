package net.minestom.server.coordinate;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(3)
public class YawBenchmark {

    @Param({"true", "false"})
    boolean inside;

    private float[] randomYaws;
    private int index = 0;

    @Setup
    public void setup() {
        randomYaws = new float[1024];
        Random r = new Random(67);
        for (int i = 0; i < randomYaws.length; i++) {
            randomYaws[i] = inside ? r.nextFloat(-179.99f, 180.0f) : r.nextFloat(-1000.0f, 1000.0f);
        }
    }

    @Benchmark
    public float benchmarkExisting() {
        float yaw = randomYaws[index++ & 1023];
        yaw = yaw % 360.0f;
        if (yaw <= -180.0f) yaw += 360.0f;
        else if (yaw > 180.0f) yaw -= 360.0f;
        return yaw;
    }


    @Benchmark
    public float benchmarkOne() {
        float yaw = randomYaws[index++ & 1023];
        while (yaw <= -180.0f) yaw += 360.0f;
        while (yaw > 180.0f) yaw -= 360.0f;
        return yaw;
    }

    @Benchmark
    public float benchmarkTwo() {
        float yaw = randomYaws[index++ & 1023];
        return yaw - 360.0f * (float) Math.ceil((yaw - 180.0f) / 360.0f);
    }
}