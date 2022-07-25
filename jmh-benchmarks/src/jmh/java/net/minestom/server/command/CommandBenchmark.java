package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.String;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static net.minestom.server.command.builder.arguments.ArgumentType.Double;
import static net.minestom.server.command.builder.arguments.ArgumentType.Float;
import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.Long;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

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
                    addSyntax((sender, context) -> {}, Potion("pos"));
                    addSyntax((sender, context) -> {}, Entity("entity"), Potion("pos"));
                }},
                new Command("setblock", "set") {{
                    addSyntax((sender, context) -> {}, RelativeBlockPosition("pos"), BlockState("block"));
                }},
                new Command("foo") {{
                    setCondition((sender, commandString) -> true);
                    addSubcommand(new Command("bar") {{
                        addConditionalSyntax((sender, commandString) -> true, (sender, context) -> {});
                    }});
                    addSubcommand(new Command("baz"){{
                        addSyntax((sender, context) -> {}, Word("A").from("a", "b", "c"), Word("B").from("a", "b", "c"));
                    }});
                }},
                new Command("def") {{
                    addSyntax((sender, context) -> {}, Literal("a"), Literal("b"), Literal("c"), Literal("d"),
                            Literal("e"), Literal("f"));
                    setDefaultExecutor((sender, context) -> {});
                }},
                new Command("parse") {{
                    addSyntax((sender, context) -> {}, Literal("int"), Integer("val"));
                    addSyntax((sender, context) -> {}, Literal("double"), Double("val"));
                    addSyntax((sender, context) -> {}, Literal("float"), Float("val"));
                    addSyntax((sender, context) -> {}, Literal("long"), Long("val"));
                }}
        ));
        final CommandParser commandParser = CommandParser.parser();
        this.parser = input -> commandParser.parse(graph, input);
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
