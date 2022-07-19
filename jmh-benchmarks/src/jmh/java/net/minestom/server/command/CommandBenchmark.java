package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.String;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.minestom.server.command.builder.arguments.ArgumentType.*;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CommandBenchmark {
    Graph graph;
    String[] inputs;

    @Setup
    public void setup() {
        this.graph = Graph.merge(Set.of(
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
                }}
        ));
        this.inputs = new String[] {
                "tp 0 a", "tp 0 0 0", "tp foo 0", "tp foo bar", "tp foo 0 0 0", "tp 1 2 3", "tp", "tp 1 2 3 4 5",
                "teleport a", "teleport bar 1 2 3 4", "teleport 1 2 3", "teleport 12 2 4", "set a", "set", "set 0 0 0",
                "set 0 0 0 air", "setblock", "setblock 0 0 0 stone", "set 0 0 0 0", "set 0 0 0 0 stone 0", "set 1 2a 3 a",
                "foo", "foo bar", "bar", "baz", "foo bar 15", "foo bar a a a a ", "foo bar a b", "foo bar baz", "foo baz A",
                "foo baz a", "foo baz a a", "foo baz a b", "foo baz a b c", "def a a a a", "def", "def a a a a a", "def a",
                "def a a a a a a a a a", "def b", "def a a b", "b", "a", "def a", "def a b", "def a b c d e"
        };
    }

    @Benchmark
    public void run(Blackhole bh) {
        final CommandParser parser = CommandParser.parser();
        for (String input : inputs) {
            bh.consume(parser.parse(graph, input).execute(null));
        }
    }
}
