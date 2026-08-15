package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static net.minestom.server.command.builder.arguments.ArgumentType.BlockState;
import static net.minestom.server.command.builder.arguments.ArgumentType.Double;
import static net.minestom.server.command.builder.arguments.ArgumentType.Entity;
import static net.minestom.server.command.builder.arguments.ArgumentType.Float;
import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static net.minestom.server.command.builder.arguments.ArgumentType.Long;
import static net.minestom.server.command.builder.arguments.ArgumentType.RelativeBlockPosition;
import static net.minestom.server.command.builder.arguments.ArgumentType.RelativeVec3;
import static net.minestom.server.command.builder.arguments.ArgumentType.Word;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(time = 2, iterations = 3)
@Measurement(time = 6)
public class CommandBenchmark {
    Function<String, Object> parser;

    @Setup
    public void setup() {
        var graph = Graph.merge(Set.of(
                new Command("tp", "teleport") {{
                    addSyntax((_, _) -> {}, RelativeVec3("pos"));
                    addSyntax((_, _) -> {}, Entity("entity"), RelativeVec3("pos"));
                }},
                new Command("setblock", "set") {{
                    addSyntax((_, _) -> {}, RelativeBlockPosition("pos"), BlockState("block"));
                }},
                new Command("foo") {{
                    setCondition((_, _) -> true);
                    addSubcommand(new Command("bar") {{
                        addConditionalSyntax((_, _) -> true, (_, _) -> {});
                    }});
                    addSubcommand(new Command("baz"){{
                        addSyntax((_, _) -> {}, Word("A").from("a", "b", "c"), Word("B").from("a", "b", "c"));
                    }});
                }},
                new Command("def") {{
                    addSyntax((_, _) -> {}, Literal("a"), Literal("b"), Literal("c"), Literal("d"),
                            Literal("e"), Literal("f"));
                    setDefaultExecutor((_, _) -> {});
                }},
                new Command("parse") {{
                    addSyntax((_, _) -> {}, Literal("int"), Integer("val"));
                    addSyntax((_, _) -> {}, Literal("double"), Double("val"));
                    addSyntax((_, _) -> {}, Literal("float"), Float("val"));
                    addSyntax((_, _) -> {}, Literal("long"), Long("val"));
                }}
        ));
        final CommandParser commandParser = CommandParser.parser();
        this.parser = input -> commandParser.parse(null, graph, input);
    }

    @Benchmark
    public void unknownCommand5Char(Blackhole bh) {
        bh.consume(parser.apply("01234"));
    }

    @Benchmark
    public void unknownCommand50Char(Blackhole bh) {
        bh.consume(parser.apply("01234567890123456789012345678901234567890123456789"));
    }

    @Benchmark
    public void validCommandWithValidLiteral(Blackhole bh) {
        bh.consume(parser.apply("foo bar"));
    }

    @Benchmark
    public void validCommandWithInvalid50CharLiteral(Blackhole bh) {
        bh.consume(parser.apply("foo 01234567890123456789012345678901234567890123456789"));
    }

    @Benchmark
    public void numberParsing3Digit(Blackhole bh) {
        bh.consume(parser.apply("parse int 123"));
        bh.consume(parser.apply("parse float 123"));
        bh.consume(parser.apply("parse double 123"));
        bh.consume(parser.apply("parse long 123"));
    }

    @Benchmark
    public void numberParsing10Digit(Blackhole bh) {
        bh.consume(parser.apply("parse int 1234567890"));
        bh.consume(parser.apply("parse float 1234567890"));
        bh.consume(parser.apply("parse double 1234567890"));
        bh.consume(parser.apply("parse long 1234567890"));
    }

    @Benchmark
    public void numberParsing10DigitInvalid(Blackhole bh) {
        bh.consume(parser.apply("parse int a1234567890"));
        bh.consume(parser.apply("parse float a1234567890"));
        bh.consume(parser.apply("parse double a1234567890"));
        bh.consume(parser.apply("parse long a1234567890"));
    }
}
