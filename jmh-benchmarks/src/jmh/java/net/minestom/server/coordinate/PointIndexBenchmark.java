package net.minestom.server.coordinate;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class PointIndexBenchmark {

    @Param({"100", "1000", "10000"})
    int size;

    @Param({"16", "64"})
    double radius;

    private PointIndex index;
    private Point[] points;
    private int cursor;

    @Setup
    public void setup() {
        index = PointIndex.create();
        Random r = new Random(42);
        points = new Point[size];
        for (int i = 0; i < size; i++) {
            points[i] = randomPoint(r);
            index.add(i, points[i]);
        }
    }

    @Benchmark
    public int nearest() {
        Point p = points[Math.floorMod(cursor++, size)];
        return index.nearest(p);
    }

    @Benchmark
    public int countWithin() {
        Point p = points[Math.floorMod(cursor++, size)];
        return index.count(p, radius);
    }

    @Benchmark
    public void forEachWithin(Blackhole bh) {
        Point p = points[Math.floorMod(cursor++, size)];
        index.forEachWithin(p, radius, bh::consume);
    }

    @Benchmark
    public void forEachInChunkRange(Blackhole bh) {
        Point p = points[Math.floorMod(cursor++, size)];
        index.forEachInChunkRange(p, 4, bh::consume);
    }

    @Benchmark
    public void moveCycle(Blackhole bh) {
        int i = Math.floorMod(cursor++, size);
        Point newPoint = new Vec(
                (cursor * 13L) % 1024 - 512,
                (cursor * 7L) % 256,
                (cursor * 17L) % 1024 - 512);
        bh.consume(index.move(i, newPoint));
        points[i] = newPoint;
    }

    private static Point randomPoint(Random r) {
        return new Vec(
                r.nextDouble(-512, 512),
                r.nextDouble(0, 256),
                r.nextDouble(-512, 512));
    }
}
