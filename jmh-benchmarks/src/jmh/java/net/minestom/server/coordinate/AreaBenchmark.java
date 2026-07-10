package net.minestom.server.coordinate;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class AreaBenchmark {

    public enum Shape {
        SINGLE(Area.single(new BlockVec(5, 5, 5))),
        LINE_AXIS(Area.line(new BlockVec(0, 0, 0), new BlockVec(128, 0, 0))),
        LINE_DIAGONAL(Area.line(new BlockVec(0, 0, 0), new BlockVec(64, 32, 96))),
        CUBOID_SECTION(Area.section(0, 0, 0)),
        CUBOID_MULTISECTION(Area.cuboid(new BlockVec(-20, -20, -20), new BlockVec(20, 20, 20))),
        SPHERE_SMALL(Area.sphere(new BlockVec(0, 0, 0), 4)),
        SPHERE_LARGE(Area.sphere(new BlockVec(0, 0, 0), 16));

        final Area area;
        final Point inside;
        final Point outside;

        Shape(Area area) {
            this.area = area;
            this.inside = area.iterator().next();
            final BlockVec max = area.bound().max();
            this.outside = max.add(1000, 1000, 1000);
        }
    }

    @Param
    public Shape shape;

    private Area area;
    private Point inside;
    private Point outside;

    @Setup
    public void setup() {
        area = shape.area;
        inside = shape.inside;
        outside = shape.outside;
    }

    @Benchmark
    public long blockCount() {
        return area.blockCount();
    }

    @Benchmark
    public void iterate(Blackhole bh) {
        for (BlockVec v : area) bh.consume(v);
    }

    @Benchmark
    public List<Area.Cuboid> split() {
        return area.split();
    }

    @Benchmark
    public boolean containsInside() {
        return area.contains(inside);
    }

    @Benchmark
    public boolean containsOutside() {
        return area.contains(outside);
    }

    @Benchmark
    public Area bound() {
        return area.bound();
    }

    @Benchmark
    public Area offset() {
        return area.offset(7, -3, 11);
    }
}
