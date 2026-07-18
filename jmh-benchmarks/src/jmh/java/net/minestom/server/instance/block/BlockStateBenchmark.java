package net.minestom.server.instance.block;

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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class BlockStateBenchmark {
    @Param({"CHEST", "OAK_STAIRS", "REDSTONE_WIRE"})
    public String blockType;

    private Block block;
    private Block changedBlock;
    private String property;
    private String unchangedValue;
    private String changedValue;
    private Map<String, String> changedProperties;
    private String state;
    private int stateId;

    @Setup
    public void setup() {
        switch (blockType) {
            case "CHEST" -> {
                block = Block.CHEST;
                property = "facing";
                changedValue = "south";
            }
            case "OAK_STAIRS" -> {
                block = Block.OAK_STAIRS;
                property = "facing";
                changedValue = "west";
            }
            case "REDSTONE_WIRE" -> {
                block = Block.REDSTONE_WIRE;
                property = "power";
                changedValue = "15";
            }
            default -> throw new IllegalArgumentException("Unknown block type: " + blockType);
        }
        unchangedValue = block.getProperty(property);
        changedProperties = Map.of(property, changedValue);
        changedBlock = block.withProperty(property, changedValue);
        state = changedBlock.state();
        stateId = changedBlock.stateId();
    }

    @Benchmark
    public Map<String, String> properties() {
        return changedBlock.properties();
    }

    @Benchmark
    public String state() {
        return changedBlock.state();
    }

    @Benchmark
    public String getProperty() {
        return block.getProperty(property);
    }

    @Benchmark
    public String getMissingProperty() {
        return block.getProperty("not_a_property");
    }

    @Benchmark
    public Block withPropertyChanged() {
        return block.withProperty(property, changedValue);
    }

    @Benchmark
    public Block withPropertyUnchanged() {
        return block.withProperty(property, unchangedValue);
    }

    @Benchmark
    public Block withPropertiesChanged() {
        return block.withProperties(changedProperties);
    }

    @Benchmark
    public Block fromState() {
        return Block.fromState(state);
    }

    @Benchmark
    public Block fromStateId() {
        return Block.fromStateId(stateId);
    }

    @Benchmark
    public int possibleStatesSize() {
        return block.possibleStates().size();
    }

    @Benchmark
    public int possibleStatesIteration() {
        final Collection<Block> possibleStates = block.possibleStates();
        int result = 1;
        for (Block possibleState : possibleStates) result = 31 * result + possibleState.stateId();
        return result;
    }
}
