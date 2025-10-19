package net.minestom.server.network;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class NetworkBufferIOUTFStringBenchmark {

    // Test strings of various types and lengths; Asked AI for some strings
    private static final String ASCII_SHORT = "Hello, World!";
    private static final String ASCII_MEDIUM = "The quick brown fox jumps over the lazy dog. ".repeat(5);
    private static final String ASCII_LONG = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(20);

    private static final String UNICODE_SHORT = "Hello 世界! 🌍";
    private static final String UNICODE_MEDIUM = "Minestom supports: English, 中文, 日本語, 한국어, العربية, Русский! 🎮🚀".repeat(3);
    private static final String UNICODE_LONG = "🎮 Gaming server with emoji 🚀 and Unicode ©®™ symbols ∑∏√∫ ".repeat(15);

    private static final String MIXED_SHORT = "Player123 joined!";
    private static final String MIXED_MEDIUM = "User€100 bought §aGreen§r item for ¥500 (税込み)".repeat(4);
    private static final String MIXED_LONG = "Server message: Player123 (レベル50) earned achievement 🏆 'Master Builder' for constructing 1,000+ blocks!".repeat(10);

    // Writing buffers
    private NetworkBuffer writeBufferAsciiShort;
    private NetworkBuffer writeBufferAsciiMedium;
    private NetworkBuffer writeBufferAsciiLong;
    private NetworkBuffer writeBufferUnicodeShort;
    private NetworkBuffer writeBufferUnicodeMedium;
    private NetworkBuffer writeBufferUnicodeLong;
    private NetworkBuffer writeBufferMixedShort;
    private NetworkBuffer writeBufferMixedMedium;
    private NetworkBuffer writeBufferMixedLong;

    // Reading buffers
    private NetworkBuffer readBufferAsciiShort;
    private NetworkBuffer readBufferAsciiMedium;
    private NetworkBuffer readBufferAsciiLong;
    private NetworkBuffer readBufferUnicodeShort;
    private NetworkBuffer readBufferUnicodeMedium;
    private NetworkBuffer readBufferUnicodeLong;
    private NetworkBuffer readBufferMixedShort;
    private NetworkBuffer readBufferMixedMedium;
    private NetworkBuffer readBufferMixedLong;

    @Setup
    public void setup() {
        // Initialize write buffers
        writeBufferAsciiShort = NetworkBuffer.resizableBuffer();
        writeBufferAsciiMedium = NetworkBuffer.resizableBuffer();
        writeBufferAsciiLong = NetworkBuffer.resizableBuffer();
        writeBufferUnicodeShort = NetworkBuffer.resizableBuffer();
        writeBufferUnicodeMedium = NetworkBuffer.resizableBuffer();
        writeBufferUnicodeLong = NetworkBuffer.resizableBuffer();
        writeBufferMixedShort = NetworkBuffer.resizableBuffer();
        writeBufferMixedMedium = NetworkBuffer.resizableBuffer();
        writeBufferMixedLong = NetworkBuffer.resizableBuffer();

        // Initialize and pre-fill read buffers
        readBufferAsciiShort = NetworkBuffer.resizableBuffer();
        readBufferAsciiShort.write(NetworkBuffer.STRING_IO_UTF8, ASCII_SHORT);

        readBufferAsciiMedium = NetworkBuffer.resizableBuffer();
        readBufferAsciiMedium.write(NetworkBuffer.STRING_IO_UTF8, ASCII_MEDIUM);

        readBufferAsciiLong = NetworkBuffer.resizableBuffer();
        readBufferAsciiLong.write(NetworkBuffer.STRING_IO_UTF8, ASCII_LONG);

        readBufferUnicodeShort = NetworkBuffer.resizableBuffer();
        readBufferUnicodeShort.write(NetworkBuffer.STRING_IO_UTF8, UNICODE_SHORT);

        readBufferUnicodeMedium = NetworkBuffer.resizableBuffer();
        readBufferUnicodeMedium.write(NetworkBuffer.STRING_IO_UTF8, UNICODE_MEDIUM);

        readBufferUnicodeLong = NetworkBuffer.resizableBuffer();
        readBufferUnicodeLong.write(NetworkBuffer.STRING_IO_UTF8, UNICODE_LONG);

        readBufferMixedShort = NetworkBuffer.resizableBuffer();
        readBufferMixedShort.write(NetworkBuffer.STRING_IO_UTF8, MIXED_SHORT);

        readBufferMixedMedium = NetworkBuffer.resizableBuffer();
        readBufferMixedMedium.write(NetworkBuffer.STRING_IO_UTF8, MIXED_MEDIUM);

        readBufferMixedLong = NetworkBuffer.resizableBuffer();
        readBufferMixedLong.write(NetworkBuffer.STRING_IO_UTF8, MIXED_LONG);
    }

    @Benchmark
    public void writeAsciiShort() {
        writeBufferAsciiShort.clear();
        writeBufferAsciiShort.write(NetworkBuffer.STRING_IO_UTF8, ASCII_SHORT);
    }

    @Benchmark
    public void writeAsciiMedium() {
        writeBufferAsciiMedium.clear();
        writeBufferAsciiMedium.write(NetworkBuffer.STRING_IO_UTF8, ASCII_MEDIUM);
    }

    @Benchmark
    public void writeAsciiLong() {
        writeBufferAsciiLong.clear();
        writeBufferAsciiLong.write(NetworkBuffer.STRING_IO_UTF8, ASCII_LONG);
    }

    @Benchmark
    public void writeUnicodeShort() {
        writeBufferUnicodeShort.clear();
        writeBufferUnicodeShort.write(NetworkBuffer.STRING_IO_UTF8, UNICODE_SHORT);
    }

    @Benchmark
    public void writeUnicodeMedium() {
        writeBufferUnicodeMedium.clear();
        writeBufferUnicodeMedium.write(NetworkBuffer.STRING_IO_UTF8, UNICODE_MEDIUM);
    }

    @Benchmark
    public void writeUnicodeLong() {
        writeBufferUnicodeLong.clear();
        writeBufferUnicodeLong.write(NetworkBuffer.STRING_IO_UTF8, UNICODE_LONG);
    }

    @Benchmark
    public void writeMixedShort() {
        writeBufferMixedShort.clear();
        writeBufferMixedShort.write(NetworkBuffer.STRING_IO_UTF8, MIXED_SHORT);
    }

    @Benchmark
    public void writeMixedMedium() {
        writeBufferMixedMedium.clear();
        writeBufferMixedMedium.write(NetworkBuffer.STRING_IO_UTF8, MIXED_MEDIUM);
    }

    @Benchmark
    public void writeMixedLong() {
        writeBufferMixedLong.clear();
        writeBufferMixedLong.write(NetworkBuffer.STRING_IO_UTF8, MIXED_LONG);
    }

    @Benchmark
    public void readAsciiShort(Blackhole blackhole) {
        readBufferAsciiShort.readIndex(0);
        blackhole.consume(readBufferAsciiShort.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void readAsciiMedium(Blackhole blackhole) {
        readBufferAsciiMedium.readIndex(0);
        blackhole.consume(readBufferAsciiMedium.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void readAsciiLong(Blackhole blackhole) {
        readBufferAsciiLong.readIndex(0);
        blackhole.consume(readBufferAsciiLong.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void readUnicodeShort(Blackhole blackhole) {
        readBufferUnicodeShort.readIndex(0);
        blackhole.consume(readBufferUnicodeShort.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void readUnicodeMedium(Blackhole blackhole) {
        readBufferUnicodeMedium.readIndex(0);
        blackhole.consume(readBufferUnicodeMedium.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void readUnicodeLong(Blackhole blackhole) {
        readBufferUnicodeLong.readIndex(0);
        blackhole.consume(readBufferUnicodeLong.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void readMixedShort(Blackhole blackhole) {
        readBufferMixedShort.readIndex(0);
        blackhole.consume(readBufferMixedShort.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void readMixedMedium(Blackhole blackhole) {
        readBufferMixedMedium.readIndex(0);
        blackhole.consume(readBufferMixedMedium.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void readMixedLong(Blackhole blackhole) {
        readBufferMixedLong.readIndex(0);
        blackhole.consume(readBufferMixedLong.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void roundTripAsciiShort(Blackhole blackhole) {
        writeBufferAsciiShort.clear();
        writeBufferAsciiShort.write(NetworkBuffer.STRING_IO_UTF8, ASCII_SHORT);
        blackhole.consume(writeBufferAsciiShort.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void roundTripUnicodeMedium(Blackhole blackhole) {
        writeBufferUnicodeMedium.clear();
        writeBufferUnicodeMedium.write(NetworkBuffer.STRING_IO_UTF8, UNICODE_MEDIUM);
        blackhole.consume(writeBufferUnicodeMedium.read(NetworkBuffer.STRING_IO_UTF8));
    }

    @Benchmark
    public void roundTripMixedLong(Blackhole blackhole) {
        writeBufferMixedLong.clear();
        writeBufferMixedLong.write(NetworkBuffer.STRING_IO_UTF8, MIXED_LONG);
        blackhole.consume(writeBufferMixedLong.read(NetworkBuffer.STRING_IO_UTF8));
    }
}

