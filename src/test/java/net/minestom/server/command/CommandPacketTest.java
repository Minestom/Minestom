package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommandPacketTest {
    @Test
    public void singleCommandWithOneSyntax() {
        final Command foo = new Command("foo");
        foo.addSyntax(CommandPacketTest::dummyExecutor, ArgumentType.Integer("bar"));

        final DeclareCommandsPacket packet = GraphConverter.createPacket(Graph.merge(Graph.fromCommand(foo)));
        assertEquals(3, packet.nodes().size());
        final DeclareCommandsPacket.Node root = packet.nodes().get(packet.rootIndex());
        assertNotNull(root);
        assertNodeType(DeclareCommandsPacket.NodeType.ROOT, root.flags);
        assertEquals(1, root.children.length);
        final DeclareCommandsPacket.Node cmd = packet.nodes().get(root.children[0]);
        assertNotNull(cmd);
        assertNodeType(DeclareCommandsPacket.NodeType.LITERAL, cmd.flags);
        assertEquals(1, cmd.children.length);
        assertEquals("foo", cmd.name);
        final DeclareCommandsPacket.Node arg = packet.nodes().get(cmd.children[0]);
        assertNotNull(arg);
        assertNodeType(DeclareCommandsPacket.NodeType.ARGUMENT, arg.flags);
        assertEquals(0, arg.children.length);
        assertEquals("bar", arg.name);
    }

    @Test
    public void executeLike() {
        enum Dimension {OVERWORLD, THE_NETHER, THE_END}
        final Command execute = new Command("execute");
        execute.addSyntax(CommandPacketTest::dummyExecutor, ArgumentType.Loop("params",
                ArgumentType.Group("facing", ArgumentType.Literal("facing"), ArgumentType.RelativeVec3("pos")),
                ArgumentType.Group("at", ArgumentType.Literal("at"), ArgumentType.Entity("targets")),
                ArgumentType.Group("as", ArgumentType.Literal("as"), ArgumentType.Entity("targets")),
                ArgumentType.Group("in", ArgumentType.Literal("in"), ArgumentType.Enum("dimesion", Dimension.class)),
                ArgumentType.Group("run", ArgumentType.Command("run"))
        ));
        var graph = Graph.merge(Graph.fromCommand(execute));
        assertPacketGraph("""
                digraph G {
                  rankdir=LR
                  12 [label="root",shape=rectangle]
                  0 [label="'facing'"]
                  0 -> { 1 }
                  1 [label="pos"]
                  1 -> { 11 } [style = dotted]
                  2 [label="'at'"]
                  2 -> { 3 }
                  3 [label="targets"]
                  3 -> { 11 } [style = dotted]
                  4 [label="'as'"]
                  4 -> { 5 }
                  5 [label="targets"]
                  5 -> { 11 } [style = dotted]
                  6 [label="'in'"]
                  6 -> { 7 8 9 }
                  7 [label="'OVERWORLD'"]
                  7 -> { 11 } [style = dotted]
                  8 [label="'THE_NETHER'"]
                  8 -> { 11 } [style = dotted]
                  9 [label="'THE_END'"]
                  9 -> { 11 } [style = dotted]
                  10 [label="'run'"]
                  10 -> { 12 } [style = dotted]
                  11 [label="'execute'"]
                  11 -> { 0 2 4 6 10 }
                  12 -> { 11 }
                }
                """, graph);
    }

    @Test
    public void singleCommandTwoEnum() {
        var graph = Graph.merge(Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Enum("bar", A.class), b -> b.append(ArgumentType.Enum("baz", B.class)))
                .build());
        assertPacketGraph("""
                digraph G {
                  rankdir=LR
                  7 [label="root",shape=rectangle]
                  0 [label="'D'"]
                  1 [label="'E'"]
                  2 [label="'F'"]
                  3 [label="'A'"]
                  3 -> { 0 1 2 }
                  4 [label="'B'"]
                  4 -> { 0 1 2 }
                  5 [label="'C'"]
                  5 -> { 0 1 2 }
                  6 [label="'foo'"]
                  6 -> { 3 4 5 }
                  7 -> { 6 }
                }
                """, graph);
    }

    @Test
    public void singleCommandCommandAfterEnum() {
        var graph = Graph.merge(Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Enum("bar", A.class), b -> b.append(ArgumentType.Command("baz")))
                .build());
        assertPacketGraph("""
                digraph G {
                  rankdir=LR
                  5 [label="root",shape=rectangle]
                  0 [label="'baz'"]
                  0 -> { 5 } [style = dotted]
                  1 [label="'A'"]
                  1 -> { 0 }
                  2 [label="'B'"]
                  2 -> { 0 }
                  3 [label="'C'"]
                  3 -> { 0 }
                  4 [label="'foo'"]
                  4 -> { 1 2 3 }
                  5 -> { 4 }
                }
                """, graph);
    }

    @Test
    public void twoCommandIntEnumInt() {
        var graph = Graph.merge(
                Graph.builder(ArgumentType.Literal("foo"))
                        .append(ArgumentType.Integer("int1"), b -> b.append(ArgumentType.Enum("test", A.class), c -> c.append(ArgumentType.Integer("int2"))))
                        .build(),
                Graph.builder(ArgumentType.Literal("bar"))
                        .append(ArgumentType.Integer("int3"), b -> b.append(ArgumentType.Enum("test", B.class), c -> c.append(ArgumentType.Integer("int4"))))
                        .build()
        );
        assertPacketGraph("""
                digraph G {
                  rankdir=LR
                  12 [label="root",shape=rectangle]
                  0 [label="int2"]
                  1 [label="'A'"]
                  1 -> { 0 }
                  2 [label="'B'"]
                  2 -> { 0 }
                  3 [label="'C'"]
                  3 -> { 0 }
                  4 [label="int1"]
                  4 -> { 1 2 3 }
                  5 [label="'foo'"]
                  5 -> { 4 }
                  6 [label="int4"]
                  7 [label="'D'"]
                  7 -> { 6 }
                  8 [label="'E'"]
                  8 -> { 6 }
                  9 [label="'F'"]
                  9 -> { 6 }
                  10 [label="int3"]
                  10 -> { 7 8 9 }
                  11 [label="'bar'"]
                  11 -> { 10 }
                  12 -> { 5 11 }
                }
                """, graph);
    }

    @Test
    public void singleCommandTwoGroupOfIntInt() {
        var graph = Graph.merge(
                Graph.builder(ArgumentType.Literal("foo"))
                        .append(ArgumentType.Group("1", ArgumentType.Integer("int1"), ArgumentType.Integer("int2")),
                                b -> b.append(ArgumentType.Group("2", ArgumentType.Integer("int3"), ArgumentType.Integer("int4"))))
                        .build());
        assertPacketGraph("""
                digraph G {
                  rankdir=LR
                  5 [label="root",shape=rectangle]
                  0 [label="int3"]
                  0 -> { 1 }
                  1 [label="int4"]
                  2 [label="int1"]
                  2 -> { 3 }
                  3 [label="int2"]
                  3 -> { 0 }
                  4 [label="'foo'"]
                  4 -> { 2 }
                  5 -> { 4 }
                }
                """, graph);
    }

    static void assertPacketGraph(String expected, Graph graph) {
        var packet = GraphConverter.createPacket(graph);
        assertEquals(expected, exportGarphvizDot(packet, true));
    }

    private static String exportGarphvizDot(DeclareCommandsPacket packet, boolean prettyPrint) {
        final StringBuilder builder = new StringBuilder();
        final char statementSeparator = ';';
        builder.append("digraph G {");
        builder.append("rankdir=LR");
        builder.append(statementSeparator);
        builder.append(packet.rootIndex());
        builder.append(" [label=\"root\",shape=rectangle]");
        builder.append(statementSeparator);
        @NotNull List<DeclareCommandsPacket.Node> nodes = packet.nodes();
        for (int i = 0; i < nodes.size(); i++) {
            DeclareCommandsPacket.Node node = nodes.get(i);
            if ((node.flags & 0x3) != 0) {
                builder.append(i);
                builder.append(" [label=");
                builder.append('"');
                if ((node.flags & 0x3) == 1) {
                    builder.append("'");
                    builder.append(node.name);
                    builder.append("'");
                } else {
                    builder.append(node.name);
                }
                builder.append('"');
                if ((node.flags & 0x4) == 0x4) {
                    builder.append(",bgcolor=gray,style=filled");
                }
                builder.append("]");
                builder.append(statementSeparator);
            }
            if (node.children.length == 0 && (node.flags & 0x8) == 0) continue;
            builder.append(i);
            builder.append(" -> { ");
            if ((node.flags & 0x8) == 0) {
                builder.append(Arrays.stream(node.children).mapToObj(Integer::toString).collect(Collectors.joining(" ")));
                builder.append(" }");
                builder.append(statementSeparator);
            } else {
                builder.append(node.redirectedNode);
                builder.append(" } [style = dotted]");
                builder.append(statementSeparator);
            }
        }
        builder.append("}");
        if (prettyPrint)
            return builder.toString()
                    .replaceFirst("\\{r", "{\n  r")
                    .replaceAll(";", "\n  ")
                    .replaceFirst(" {2}}$", "}\n");
        else
            return builder.toString();
    }

    enum A {A, B, C}

    enum B {D, E, F}

    enum C {G, H, I, J, K}

    private static void assertNodeType(DeclareCommandsPacket.NodeType expected, byte flags) {
        assertEquals(expected, DeclareCommandsPacket.NodeType.values()[flags & 0x03]);
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
