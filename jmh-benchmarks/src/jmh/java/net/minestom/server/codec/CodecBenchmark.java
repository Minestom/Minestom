package net.minestom.server.codec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class CodecBenchmark {

    public enum TranscoderKind {
        NBT(Transcoder.NBT),
        JSON(Transcoder.JSON),
        JAVA(Transcoder.JAVA);

        final Transcoder<?> transcoder;

        TranscoderKind(Transcoder<?> transcoder) {
            this.transcoder = transcoder;
        }
    }

    public enum CodecKind {
        INT(Codec.INT, 42),
        STRING(Codec.STRING, "Hello, World!"),
        OPTIONAL_PRESENT(Codec.STRING.optional(), "value"),
        OPTIONAL_ABSENT(Codec.STRING.optional(), null),
        ENUM(Codec.Enum(SampleEnum.class), SampleEnum.BAR),
        LIST_INT(Codec.INT.list(), List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
        MAP_STRING_INT(Codec.STRING.mapValue(Codec.INT), Map.of("a", 1, "b", 2, "c", 3, "d", 4)),
        STRUCT_SMALL(SmallStruct.CODEC, new SmallStruct(7, "name")),
        STRUCT_LARGE(LargeStruct.CODEC, new LargeStruct(1, 2L, 3.0f, 4.0, "five", true, List.of(6, 7), "eight", 9)),
        STRUCT_NESTED(NestedStruct.CODEC, new NestedStruct("outer", new SmallStruct(1, "inner")));

        final Codec<Object> codec;
        final Object value;

        @SuppressWarnings({"rawtypes", "unchecked"})
        CodecKind(Codec codec, Object value) {
            this.codec = codec;
            this.value = value;
        }
    }

    @Param
    public CodecKind codec;

    @Param
    public TranscoderKind transcoder;

    private Transcoder<Object> activeTranscoder;
    private Object encoded;

    @Setup
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setup() {
        this.activeTranscoder = (Transcoder) transcoder.transcoder;
        this.encoded = codec.codec.encode(activeTranscoder, codec.value).orElseThrow();
    }

    @Benchmark
    public void encode(Blackhole blackhole) {
        blackhole.consume(codec.codec.encode(activeTranscoder, codec.value));
    }

    @Benchmark
    public void decode(Blackhole blackhole) {
        blackhole.consume(codec.codec.decode(activeTranscoder, encoded));
    }

    public enum SampleEnum {FOO, BAR, BAZ}

    public record SmallStruct(int id, String name) {
        public static final StructCodec<SmallStruct> CODEC = StructCodec.struct(
                "id", Codec.INT, SmallStruct::id,
                "name", Codec.STRING, SmallStruct::name,
                SmallStruct::new);
    }

    public record LargeStruct(int a, long b, float c, double d, String e, boolean f, List<Integer> g, String h, int i) {
        public static final StructCodec<LargeStruct> CODEC = StructCodec.struct(
                "a", Codec.INT, LargeStruct::a,
                "b", Codec.LONG, LargeStruct::b,
                "c", Codec.FLOAT, LargeStruct::c,
                "d", Codec.DOUBLE, LargeStruct::d,
                "e", Codec.STRING, LargeStruct::e,
                "f", Codec.BOOLEAN, LargeStruct::f,
                "g", Codec.INT.list(), LargeStruct::g,
                "h", Codec.STRING, LargeStruct::h,
                "i", Codec.INT, LargeStruct::i,
                LargeStruct::new);
    }

    public record NestedStruct(String label, SmallStruct inner) {
        public static final StructCodec<NestedStruct> CODEC = StructCodec.struct(
                "label", Codec.STRING, NestedStruct::label,
                "inner", SmallStruct.CODEC, NestedStruct::inner,
                NestedStruct::new);
    }
}
