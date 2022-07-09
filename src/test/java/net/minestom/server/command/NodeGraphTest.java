package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket.NodeType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("SpellCheckingInspection")
public class NodeGraphTest {

    @Test
    public void commandToPacket() {
        final Command foo = new Command("foo");
        foo.addSyntax(NodeGraphTest::dummyExecutor, ArgumentType.Integer("bar"));

        final DeclareCommandsPacket packet = GraphConverter.createPacket(GraphBuilder.forServer(Set.of(foo)));
        assertEquals(3, packet.nodes().size());
        final DeclareCommandsPacket.Node root = packet.nodes().get(packet.rootIndex());
        assertNotNull(root);
        assertNodeType(NodeType.ROOT, root.flags);
        assertEquals(1, root.children.length);
        final DeclareCommandsPacket.Node cmd = packet.nodes().get(root.children[0]);
        assertNotNull(cmd);
        assertNodeType(NodeType.LITERAL, cmd.flags);
        assertEquals(1, cmd.children.length);
        assertEquals("foo", cmd.name);
        final DeclareCommandsPacket.Node arg = packet.nodes().get(cmd.children[0]);
        assertNotNull(arg);
        assertNodeType(NodeType.ARGUMENT, arg.flags);
        assertEquals(0, arg.children.length);
        assertEquals("bar", arg.name);
    }

    @Test
    public void graphTest() {
        final Command foo = new Command("foo");
        foo.addSyntax(NodeGraphTest::dummyExecutor, ArgumentType.Literal("bar"));
        enum A{A,B,C,D,E}
        foo.addSyntax(NodeGraphTest::dummyExecutor, ArgumentType.Literal("baz"), ArgumentType.Enum("a", A.class));
        assertEquals("""
                digraph G {
                  rankdir=LR
                  0 [label="root",shape=rectangle]
                  0 -> { 1 }
                  1 [label="foo"]
                  1 -> { 2 3 }
                  2 [label="bar",bgcolor=gray,style=filled]
                  3 [label="baz"]
                  3 -> { 4 5 6 7 8 }
                  4 [label="A",bgcolor=gray,style=filled]
                  5 [label="B",bgcolor=gray,style=filled]
                  6 [label="C",bgcolor=gray,style=filled]
                  7 [label="D",bgcolor=gray,style=filled]
                  8 [label="E",bgcolor=gray,style=filled]
                }
                """, GraphBuilder.forServer(Set.of(foo)).exportGarphvizDot(true));
    }

    private static void assertNodeType(NodeType expected, byte flags) {
        assertEquals(expected, NodeType.values()[flags & 0x03]);
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {

    }
}
