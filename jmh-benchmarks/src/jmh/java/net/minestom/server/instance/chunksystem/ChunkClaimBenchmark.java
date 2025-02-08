package net.minestom.server.instance.chunksystem;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ChunkClaimBenchmark {
    @State(Scope.Thread)
    public static class InsertState {
        @Param({"10", "100", "500"})
        int clusters;
        @Param({"50", "500", "2000"})
        int clusterSize;
    }

    @Benchmark
    public void insertClaims(Blackhole blackhole, InsertState state) {
        ChunkClaimTree tree = new ChunkClaimTree();
        var random = new Random();
        for (var cluster = 0; cluster < state.clusters; cluster++) {
            var clusterX = cluster * 1000;
            var clusterZ = random.nextInt(100000);

            for (var i = 0; i < state.clusterSize; i++) {
                var deltaX = random.nextInt(100);
                var deltaZ = random.nextInt(100);
                var x = clusterX + deltaX;
                var z = clusterZ + deltaZ;

                tree.insert(x, z, 32, 50, ChunkClaim.Shape.CIRCLE);
            }
        }
        blackhole.consume(tree);
        blackhole.consume(state);
    }

}
