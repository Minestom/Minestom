package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandPacketTest {

    @Test
    public void singleCommandWithOneSyntax() {
        final Command foo = new Command("foo");
        foo.addSyntax(CommandPacketTest::dummyExecutor, ArgumentType.Integer("bar"));

        final DeclareCommandsPacket packet = GraphConverter.createPacket(new CommandManager(), Graph.merge(Graph.fromCommand(foo)), null);
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
        assertExecutable(arg.flags);
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
                ArgumentType.Group("in", ArgumentType.Literal("in"), ArgumentType.Enum("dimension", Dimension.class)),
                ArgumentType.Group("run", ArgumentType.Command("run"))
        ));
        var graph = Graph.fromCommand(execute);
        assertPacketGraph("""
                execute facing at as in run=%
                overworld the_nether the_end=§
                0->execute
                atEnt asEnt=targets ENTITY 0
                execute->facing at as in run
                at->atEnt
                as->asEnt
                in->overworld the_nether the_end
                pos=! VEC3
                facing->pos
                pos atEnt asEnt overworld the_nether the_end+>execute
                run+>0
                """, graph);
    }

    @Test
    public void singleCommandTwoEnum() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Enum("bar", Letter.class), b -> b.append(ArgumentType.Enum("baz", B.class)))
                .build();
        assertPacketGraph("""
                foo=%
                a b c d e f=§
                0->foo
                foo->a b c
                a b c->d e f
                """, graph);
    }

    @Test
    public void singleCommandMappedLiteral() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Literal("bar").map(String::length))
                .build();
        assertPacketGraph("""
                foo bar=%
                0->foo
                foo->bar
                """, graph);
    }

    @Test
    public void singleCommandFilteredLiteral() {
        // Filters only apply when parsing, so the literal is still sent even though it can never match
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Literal("bar").filter(_ -> false))
                .build();
        assertPacketGraph("""
                foo bar=%
                0->foo
                foo->bar
                """, graph);
    }

    @Test
    public void singleCommandMappedEnum() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Enum("bar", Letter.class).map(Enum::name))
                .build();
        assertPacketGraph("""
                foo=%
                a b c=§
                0->foo
                foo->a b c
                """, graph);
    }

    @Test
    public void singleCommandFilteredEnum() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Enum("bar", Letter.class).filter(letter -> letter != Letter.B))
                .build();
        assertPacketGraph("""
                foo=%
                a b c=§
                0->foo
                foo->a b c
                """, graph);
    }

    @Test
    public void singleCommandRestrictedWord() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Word("bar").from("A", "B", "C"))
                .build();
        assertPacketGraph("""
                foo=%
                a b c=§
                0->foo
                foo->a b c
                """, graph);
    }

    @Test
    public void singleCommandMappedRestrictedWord() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Word("bar").from("A", "B", "C").map(String::length))
                .build();
        assertPacketGraph("""
                foo=%
                a b c=§
                0->foo
                foo->a b c
                """, graph);
    }

    @Test
    public void singleCommandFilteredRestrictedWord() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Word("bar").from("A", "B", "C").filter(word -> !word.equals("B")))
                .build();
        assertPacketGraph("""
                foo=%
                a b c=§
                0->foo
                foo->a b c
                """, graph);
    }

    @Test
    public void singleCommandChainedRestrictedWordWrappers() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Word("colour").from("red", "green", "blue")
                        .map(String::length).filter(length -> length > 3).map(String::valueOf))
                .build();
        assertPacketGraph("""
                foo=%
                red green blue=%
                0->foo
                foo->red green blue
                """, graph);
    }

    @Test
    public void singleCommandWrappedEntityTypeSuggestions() {
        var unwrapped = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.EntityType("entity"))
                .build();
        var mapped = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.EntityType("entity").map(_ -> 0))
                .build();
        var filtered = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.EntityType("entity").filter(_ -> true))
                .build();
        var expected = """
                foo=%
                entity=! RESOURCE_LOCATION minecraft:summonable_entities
                0->foo
                foo->entity
                """;
        assertPacketGraph(expected, unwrapped);
        assertPacketGraph(expected, mapped);
        assertPacketGraph(expected, filtered);
    }

    @Test
    public void singleCommandWrappedSuggestionCallback() {
        var mapped = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Word("bar").setSuggestionCallback((_, _, _) -> {}).map(String::length))
                .build();
        var filtered = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Word("bar").setSuggestionCallback((_, _, _) -> {}).filter(_ -> true))
                .build();
        var expected = """
                foo=%
                bar=! STRING 0 minecraft:ask_server
                0->foo
                foo->bar
                """;
        assertPacketGraph(expected, mapped);
        assertPacketGraph(expected, filtered);
    }

    @Test
    public void singleCommandWord() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Word("bar"))
                .build();
        assertPacketGraph("""
                foo=%
                bar=! STRING 0
                0->foo
                foo->bar
                """, graph);
    }

    @Test
    public void singleCommandMappedGroup() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Group("group", ArgumentType.Literal("bar"), ArgumentType.Word("baz")).map(_ -> 0))
                .build();
        assertPacketGraph("""
                foo bar=%
                baz=! STRING 0
                0->foo
                foo->bar
                bar->baz
                """, graph);
    }

    @Test
    public void singleCommandFilteredGroup() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Group("group", ArgumentType.Literal("bar"), ArgumentType.Word("baz")).filter(_ -> true))
                .build();
        assertPacketGraph("""
                foo bar=%
                baz=! STRING 0
                0->foo
                foo->bar
                bar->baz
                """, graph);
    }

    @Test
    public void singleCommandGroupWithMappedRestrictedWord() {
        var unwrapped = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Group("group", ArgumentType.Literal("set"), ArgumentType.Word("colour").from("red", "green", "blue")))
                .build();
        var wrapped = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Group("group", ArgumentType.Literal("set"), ArgumentType.Word("colour").from("red", "green", "blue").map(String::length)))
                .build();
        var expected = """
                foo set red green blue=%
                0->foo
                foo->set
                set->red green blue
                """;
        assertPacketGraph(expected, unwrapped);
        assertPacketGraph(expected, wrapped);
    }

    @Test
    public void singleCommandMappedLoop() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Loop("loop", ArgumentType.Word("word")).map(_ -> 0))
                .build();
        assertPacketGraph("""
                foo=%
                word=! STRING 0
                0->foo
                foo->word
                word+>foo
                """, graph);
    }

    @Test
    public void singleCommandFilteredLoop() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Loop("loop", ArgumentType.Word("word")).filter(_ -> true))
                .build();
        assertPacketGraph("""
                foo=%
                word=! STRING 0
                0->foo
                foo->word
                word+>foo
                """, graph);
    }

    @Test
    public void singleCommandLoopWithMappedRestrictedWord() {
        var unwrapped = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Loop("loop", ArgumentType.Word("colour").from("red", "green", "blue")))
                .build();
        var wrapped = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Loop("loop", ArgumentType.Word("colour").from("red", "green", "blue").map(String::length)))
                .build();
        var expected = """
                foo red green blue=%
                0->foo
                foo->red green blue
                red green blue+>foo
                """;
        assertPacketGraph(expected, unwrapped);
        assertPacketGraph(expected, wrapped);
    }

    @Test
    public void singleCommandMappedCommand() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Command("cmd").map(_ -> 0))
                .build();
        assertPacketGraph("""
                foo cmd=%
                0->foo
                foo->cmd
                cmd+>0
                """, graph);
    }

    @Test
    public void singleCommandFilteredCommand() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Command("cmd").filter(_ -> true))
                .build();
        assertPacketGraph("""
                foo cmd=%
                0->foo
                foo->cmd
                cmd+>0
                """, graph);
    }

    @Test
    public void singleCommandCommandAfterEnum() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Enum("bar", Letter.class), b -> b.append(ArgumentType.Command("baz")))
                .build();
        assertPacketGraph("""
                foo baz=%
                a b c=§
                0->foo
                foo->a b c
                a b c->baz
                baz+>0
                """, graph);
    }

    @Test
    public void twoCommandIntEnumInt() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Integer("int1"), b -> b.append(ArgumentType.Enum("test", Letter.class), c -> c.append(ArgumentType.Integer("int2"))))
                .build();
        var graph2 = Graph.builder(ArgumentType.Literal("bar"))
                .append(ArgumentType.Integer("int3"), b -> b.append(ArgumentType.Enum("test", B.class), c -> c.append(ArgumentType.Integer("int4"))))
                .build();
        assertPacketGraph("""
                foo bar=%
                0->foo bar
                a b c d e f=§
                int1 int2 int3 int4=! INTEGER 0
                foo->int1
                bar->int3
                int1->a b c
                int3->d e f
                a b c->int2
                d e f->int4
                """, graph, graph2);
    }

    @Test
    public void singleCommandTwoGroupOfIntInt() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Group("1", ArgumentType.Integer("int1"), ArgumentType.Integer("int2")),
                        b -> b.append(ArgumentType.Group("2", ArgumentType.Integer("int3"), ArgumentType.Integer("int4"))))
                .build();
        assertPacketGraph("""
                foo=%
                int1 int2 int3 int4=! INTEGER 0
                0->foo
                foo->int1
                int1->int2
                int2->int3
                int3->int4
                """, graph);
    }
    @Test
    public void twoEnumAndOneLiteralChild() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Enum("a", Letter.class))
                .append(ArgumentType.Literal("l"))
                .append(ArgumentType.Enum("b", B.class))
                .build();
        assertPacketGraph("""
                foo l=%
                0->foo
                a b c d e f=§
                foo->a b c d e f l
                """, graph);
    }

    @Test
    public void commandAliasWithoutArg() {
        var graph = Graph.builder(ArgumentType.Word("foo").from("foo", "bar"))
                .build();
        assertPacketGraph("""
                foo bar=%
                0->foo bar
                """, graph);
    }

    @Test
    public void commandAliasWithArg() {
        var graph = Graph.builder(ArgumentType.Word("foo").from("foo", "bar"))
                .append(ArgumentType.Literal("l"))
                .build();
        assertPacketGraph("""
                foo bar l=%
                0->foo bar
                foo bar->l
                """, graph);
    }

    @Test
    public void cmdArgShortcut() {
        var foo = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.String("msg"))
                .build();
        var bar = Graph.builder(ArgumentType.Literal("bar"))
                .append(ArgumentType.Command("cmd").setShortcut("foo"))
                .build();
        assertPacketGraph("""
                foo bar cmd=%
                0->foo bar
                bar->cmd
                cmd+>foo
                foo->msg
                msg=! STRING 1
                """, foo, bar);
    }

    @Test
    public void cmdArgShortcutWithPartialArg() {
        var foo = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.String("msg"))
                .build();
        var bar = Graph.builder(ArgumentType.Literal("bar"))
                .append(ArgumentType.Command("cmd").setShortcut("foo \"prefix "))
                .build();
        assertPacketGraph("""
                foo bar cmd=%
                0->foo bar
                bar->cmd
                cmd+>foo
                foo->msg
                msg=! STRING 1
                """, foo, bar);
    }

    static void assertPacketGraph(String expected, Graph... graphs) {
        var packet = GraphConverter.createPacket(new CommandManager(), Graph.merge(graphs), null);
        CommandTestUtils.assertPacket(packet, expected);
    }

    enum Letter {A, B, C}

    enum B {D, E, F}

    enum C {G, H, I, J, K}

    private static void assertNodeType(DeclareCommandsPacket.NodeType expected, byte flags) {
        assertEquals(expected, DeclareCommandsPacket.NodeType.values()[flags & 0x03]);
    }

    private static void assertExecutable(byte flags) {
        assertTrue((flags & DeclareCommandsPacket.IS_EXECUTABLE) != 0);
    }

    private static void dummyExecutor(CommandSender sender, CommandContext context) {
    }
}
