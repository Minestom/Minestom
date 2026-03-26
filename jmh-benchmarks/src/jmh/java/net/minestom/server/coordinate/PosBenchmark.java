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
public class PosBenchmark {

    @Param({"true", "false"})
    boolean inside;

    @Param({"1024", "4096"})
    int sampleSize;

    private int sampleBound;

    private float[] randomPitches;
    private float[] randomYaws;
    private int pitchIndex = 0;
    private int yawIndex = 0;

    @Setup
    public void setup() {
        final int sampleSize = this.sampleSize;
        sampleBound = sampleSize - 1;
        randomPitches = new float[sampleSize];
        randomYaws = new float[sampleSize];

        Random r = new Random(67);
        for (int i = 0; i < randomPitches.length; i++) {
            randomPitches[i] = inside ? r.nextFloat(-90f, 90.0f) : r.nextFloat(-1000.0f, 1000.0f);
        }
        for (int i = 0; i < randomYaws.length; i++) {
            randomYaws[i] = inside ? r.nextFloat(-179.99f, 180.0f) : r.nextFloat(-1000.0f, 1000.0f);
        }
    }

    @Benchmark
    public float fixYaw() {
        float yaw = randomYaws[pitchIndex++ & sampleBound];
        return Pos.fixYaw(yaw);
    }

    @Benchmark
    public float fixPitch() {
        float pitch = randomPitches[yawIndex++ & sampleBound];
        return Pos.fixPitch(pitch);
    }
}