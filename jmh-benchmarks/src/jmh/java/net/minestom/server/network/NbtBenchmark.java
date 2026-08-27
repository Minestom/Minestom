package net.minestom.server.network;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.minestom.server.network.NetworkBuffer.NBT;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class NbtBenchmark {

    @Param({"scalar", "blockEntity", "item", "heightmap", "list", "deep"})
    public String payload;

    private BinaryTag tag;
    private NetworkBuffer writeBuffer;
    private NetworkBuffer readBuffer;

    @Setup
    public void setup() {
        tag = switch (payload) {
            case "scalar" -> StringBinaryTag.stringBinaryTag("minecraft:diamond_sword");
            case "blockEntity" -> blockEntity();
            case "item" -> item();
            case "heightmap" -> heightmap();
            case "list" -> list();
            case "deep" -> deep();
            default -> throw new IllegalArgumentException("Unknown payload: " + payload);
        };
        final long size = NBT.sizeOf(tag);
        writeBuffer = NetworkBuffer.resizableBuffer((int) size);
        readBuffer = NetworkBuffer.resizableBuffer((int) size);
        readBuffer.write(NBT, tag);
    }

    @Benchmark
    public void write() {
        writeBuffer.clear();
        writeBuffer.write(NBT, tag);
    }

    @Benchmark
    public BinaryTag read() {
        readBuffer.readIndex(0);
        return readBuffer.read(NBT);
    }

    private static BinaryTag blockEntity() {
        return CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .putInt("x", 128)
                .putInt("y", 64)
                .putInt("z", -256)
                .putByte("keepPacked", (byte) 0)
                .build();
    }

    private static BinaryTag item() {
        final List<BinaryTag> lore = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            lore.add(CompoundBinaryTag.builder()
                    .putString("text", "Lore line " + i)
                    .putString("color", "gray")
                    .putBoolean("italic", false)
                    .build());
        }
        return CompoundBinaryTag.builder()
                .putString("id", "minecraft:diamond_sword")
                .putInt("count", 1)
                .put("components", CompoundBinaryTag.builder()
                        .put("minecraft:custom_name", CompoundBinaryTag.builder()
                                .putString("text", "Excalibur")
                                .putString("color", "#FFAA00")
                                .build())
                        .put("minecraft:lore", ListBinaryTag.listBinaryTag(lore.getFirst().type(), lore))
                        .put("minecraft:custom_data", CompoundBinaryTag.builder()
                                .putInt("owner", 42)
                                .putLong("createdAt", 1_700_000_000_000L)
                                .putIntArray("stats", new int[]{1, 2, 3, 4, 5, 6, 7, 8})
                                .build())
                        .putInt("minecraft:damage", 37)
                        .build())
                .build();
    }

    private static BinaryTag heightmap() {
        final long[] values = new long[256];
        for (int i = 0; i < values.length; i++) values[i] = 0x0123456789ABCDEFL ^ i;
        return CompoundBinaryTag.builder()
                .putString("type", "MOTION_BLOCKING")
                .putLongArray("data", values)
                .build();
    }

    private static BinaryTag list() {
        final List<BinaryTag> entries = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            entries.add(CompoundBinaryTag.builder()
                    .putString("name", "minecraft:entry_" + i)
                    .putInt("id", i)
                    .putFloat("weight", i * 0.25f)
                    .build());
        }
        return ListBinaryTag.listBinaryTag(entries.getFirst().type(), entries);
    }

    private static BinaryTag deep() {
        BinaryTag tag = CompoundBinaryTag.builder().putString("leaf", "bottom").build();
        for (int i = 0; i < 64; i++) tag = CompoundBinaryTag.builder().put("child", tag).build();
        return tag;
    }
}
