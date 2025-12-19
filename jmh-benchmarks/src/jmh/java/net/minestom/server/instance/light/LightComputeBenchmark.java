package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class LightComputeBenchmark {

    private Palette airPalette;
    private Palette stonePalette;
    private Palette glowstonePalette;
    private Palette mixedGlowstonePalette;
    private Palette mixedStonePalette;

    private byte[] content1;
    private byte[] content2;

    @Setup
    public void setup() {
        airPalette = Palette.blocks();
        airPalette.fill(Block.AIR.stateId());

        stonePalette = Palette.blocks();
        stonePalette.fill(Block.STONE.stateId());

        glowstonePalette = Palette.blocks();
        glowstonePalette.fill(Block.GLOWSTONE.stateId());

        mixedStonePalette = Palette.blocks();
        mixedStonePalette.fill(Block.STONE.stateId());
        for (int x = 0; x < 16; x += 2) {
            for (int y = 0; y < 16; y += 2) {
                for (int z = 0; z < 16; z += 2) {
                    mixedStonePalette.set(x, y, z, Block.AIR.stateId());
                }
            }
        }

        mixedGlowstonePalette = Palette.blocks();
        mixedGlowstonePalette.fill(Block.GLOWSTONE.stateId());
        for (int x = 0; x < 16; x += 2) {
            for (int y = 0; y < 16; y += 2) {
                for (int z = 0; z < 16; z += 2) {
                    mixedGlowstonePalette.set(x, y, z, Block.AIR.stateId());
                }
            }
        }

        // Content arrays for bake_differentArrays benchmark
        var queue1 = new ShortArrayFIFOQueue();
        queue1.enqueue((short) ((8 | (8 << 4) | (8 << 8)) | (15 << 12)));
        var queue2 = new ShortArrayFIFOQueue();
        for (int i = 0; i < 16; i += 4) {
            queue2.enqueue((short) ((i | (8 << 4) | (8 << 8)) | (15 << 12)));
        }
        content1 = LightCompute.compute(airPalette, queue1);
        content2 = LightCompute.compute(airPalette, queue2);
    }

    @Benchmark
    public void buildInternalQueue_air(Blackhole blackhole) {
        var queue = BlockLight.buildInternalQueue(airPalette);
        blackhole.consume(queue);
    }

    @Benchmark
    public void buildInternalQueue_stone(Blackhole blackhole) {
        var queue = BlockLight.buildInternalQueue(stonePalette);
        blackhole.consume(queue);
    }

    @Benchmark
    public void buildInternalQueue_glowstone(Blackhole blackhole) {
        var queue = BlockLight.buildInternalQueue(glowstonePalette);
        blackhole.consume(queue);
    }

    @Benchmark
    public void buildInternalQueue_mixedStone(Blackhole blackhole) {
        var queue = BlockLight.buildInternalQueue(mixedStonePalette);
        blackhole.consume(queue);
    }

    @Benchmark
    public void buildInternalQueue_mixedGlowStone(Blackhole blackhole) {
        var queue = BlockLight.buildInternalQueue(mixedGlowstonePalette);
        blackhole.consume(queue);
    }

    @Benchmark
    public void bake_emptyContent(Blackhole blackhole) {
        byte[] result = LightCompute.bake(LightCompute.EMPTY_CONTENT, LightCompute.EMPTY_CONTENT);
        blackhole.consume(result);
    }

    @Benchmark
    public void bake_fullyLit(Blackhole blackhole) {
        byte[] result = LightCompute.bake(LightCompute.CONTENT_FULLY_LIT, LightCompute.EMPTY_CONTENT);
        blackhole.consume(result);
    }

    @Benchmark
    public void bake_sameReference(Blackhole blackhole) {
        byte[] content = new byte[LightCompute.LIGHT_LENGTH];
        byte[] result = LightCompute.bake(content, content);
        blackhole.consume(result);
    }

    @Benchmark
    public void bake_differentArrays(Blackhole blackhole) {
        byte[] result = LightCompute.bake(content1, content2);
        blackhole.consume(result);
    }
}

