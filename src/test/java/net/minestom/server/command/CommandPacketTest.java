package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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
                ArgumentType.Group("in", ArgumentType.Literal("in"), ArgumentType.Enum("dimension", Dimension.class)),
                ArgumentType.Group("run", ArgumentType.Command("run"))
        ));
        var graph = Graph.fromCommand(execute);
        assertPacketGraph("""
                execute facing at as in run=%
                overworld the_nether the_end=§
                0->execute
                atEnt asEnt=targets minecraft:entity 0
                execute->facing at as in run
                at->atEnt
                as->asEnt
                in->overworld the_nether the_end
                pos=! minecraft:vec3
                facing->pos
                pos atEnt asEnt overworld the_nether the_end+>execute
                run+>0
                """, graph);
    }

    @Test
    public void singleCommandTwoEnum() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Enum("bar", A.class), b -> b.append(ArgumentType.Enum("baz", B.class)))
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
    public void singleCommandWord() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Word("bar"))
                .build();
        assertPacketGraph("""
                foo=%
                bar=! brigadier:string 0
                0->foo
                foo->bar
                """, graph);
    }

    @Test
    public void singleCommandCommandAfterEnum() {
        var graph = Graph.builder(ArgumentType.Literal("foo"))
                .append(ArgumentType.Enum("bar", A.class), b -> b.append(ArgumentType.Command("baz")))
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
                .append(ArgumentType.Integer("int1"), b -> b.append(ArgumentType.Enum("test", A.class), c -> c.append(ArgumentType.Integer("int2"))))
                .build();
        var graph2 = Graph.builder(ArgumentType.Literal("bar"))
                .append(ArgumentType.Integer("int3"), b -> b.append(ArgumentType.Enum("test", B.class), c -> c.append(ArgumentType.Integer("int4"))))
                .build();
        assertPacketGraph("""
                foo bar=%
                0->foo bar
                a b c d e f=§
                int1 int2 int3 int4=! brigadier:integer 0
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
                int1 int2 int3 int4=! brigadier:integer 0
                0->foo
                foo->int1
                int1->int2
                int2->int3
                int3->int4
                """, graph);
    }

    static void assertPacketGraph(String expected, Graph... graphs) {
        var packet = GraphConverter.createPacket(Graph.merge(graphs));
        final List<TestNode> expectedList = fromString("0\n0=$root$\n" + expected);
        final List<TestNode> actualList = fromString(packetToString(packet));
        assertEquals(expectedList.size(), actualList.size(), "Different node counts");
        assertTrue(actualList.containsAll(expectedList), "Packet doesn't contain all expected nodes.");
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

    private record TestNode(List<String> children, String meta, AtomicReference<String> redirect) {
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TestNode that) {
                return this.meta.equals(that.meta) && Objects.equals(this.redirect.get(), that.redirect.get()) &&
                        this.children.containsAll(that.children) && this.children.size() == that.children.size();
            } else {
                return false;
            }
        }
    }

    private static String packetToString(DeclareCommandsPacket packet) {
        final char lineSeparator = '\n';
        final StringBuilder builder = new StringBuilder();
        builder.append(packet.rootIndex());
        builder.append(lineSeparator);
        @NotNull List<DeclareCommandsPacket.Node> nodes = packet.nodes();
        for (int i = 0; i < nodes.size(); i++) {
            DeclareCommandsPacket.Node node = nodes.get(i);
            builder.append(i);
            builder.append('=');
            // Meta
            if ((node.flags & 0x3) == 0) {
                builder.append("$root$");
            } else {
                if ((node.flags & 0x3) == 1) {
                    builder.append("'");
                    builder.append(node.name);
                    builder.append("'");
                } else {
                    builder.append(node.name);
                    builder.append(' ');
                    builder.append(node.parser);

                    if (node.properties != null) {
                        builder.append(' ');
                        builder.append(new BigInteger(node.properties).toString(16));
                    }
                }
            }
            if ((node.flags & 0x4) == 0x4) {
                builder.append(" executable");
            }
            if ((node.flags & 0x10) == 0x10) {
                builder.append(' ');
                builder.append(node.suggestionsType);
            }
            builder.append(lineSeparator);
            if (node.children.length > 0) {
                builder.append(i);
                builder.append("->");
                builder.append(Arrays.stream(node.children).mapToObj(String::valueOf).collect(Collectors.joining(" ")));
                builder.append(lineSeparator);
            }
            if ((node.flags & 0x8) == 0x8) {
                builder.append(i);
                builder.append("+>");
                builder.append(node.redirectedNode);
                builder.append(lineSeparator);
            }
        }
        return builder.toString();
    }


    private static final Map<Character, Function<String, Collection<String>>> functions = Map.of(
            '!', s -> {
                final String[] strings = splitDeclaration(s);
                final ArrayList<String> result = new ArrayList<>();
                for (String s1 : strings[0].split(" ")) {
                    result.add(s1+"="+(strings[1].replaceAll("!", s1)));
                }
                return result;
            },
            '%', s -> {
                final String[] strings = splitDeclaration(s);
                final ArrayList<String> result = new ArrayList<>();
                for (String s1 : strings[0].split(" ")) {
                    result.add(s1+"="+(strings[1].replaceAll("%", "'"+s1+"'")));
                }
                return result;
            },
            '§', s -> {
                final String[] strings = splitDeclaration(s);
                final ArrayList<String> result = new ArrayList<>();
                for (String s1 : strings[0].split(" ")) {
                    result.add(s1+"="+(strings[1].replaceAll("§", "'"+(s1.toUpperCase(Locale.ROOT))+"'")));
                }
                return result;
            }
    );
    private static final Set<Character> placeholders = functions.keySet();

    private static String[] splitDeclaration(String input) {
        return input.split("=", 2);
    }

    private static List<String> preProcessString(String string) {
        final List<String> strings = Arrays.stream(string.split("\n")).toList();
        final ArrayList<String> result = new ArrayList<>();
        for (String s : strings) {
            if (s.indexOf('=') > -1) {
                boolean match = false;
                for (Character placeholder : placeholders) {
                    if (s.indexOf(placeholder) > -1) {
                        result.addAll(functions.get(placeholder).apply(s));
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    final int spaceIndex = s.indexOf(" ");
                    if (spaceIndex > -1 && spaceIndex < s.indexOf('=')) {
                        final String[] split = s.split("=", 2);
                        for (String s1 : split[0].split(" ")) {
                            result.add(s1+"="+split[1]);
                        }
                    } else {
                        result.add(s);
                    }
                }
            } else {
                final int spaceIndex = s.indexOf(" ");
                if (spaceIndex > -1 && spaceIndex < s.indexOf('-')) {
                    final String[] split = s.split("-", 2);
                    for (String s1 : split[0].split(" ")) {
                        result.add(s1+"-"+split[1]);
                    }
                } else if (spaceIndex > -1 && spaceIndex < s.indexOf('+')) {
                    final String[] split = s.split("\\+", 2);
                    for (String s1 : split[0].split(" ")) {
                        result.add(s1+"+"+split[1]);
                    }
                } else {
                    result.add(s);
                }
            }
        }
        return result;
    }

    private static List<TestNode> fromString(String input) {
        Map<String, String[]> references = new HashMap<>();
        Map<String, TestNode> nodes = new HashMap<>();
        final List<String> strings = preProcessString(input);
        String rootId = strings.get(0);

        for (String s : strings.stream().skip(0).toList()) {
            if (s.length() < 3) continue; //invalid line
            final int declareSeparator = s.indexOf('=');
            if (declareSeparator > -1) {
                final String id = s.substring(0, declareSeparator);
                final String meta = s.substring(declareSeparator + 1);
                nodes.put(id, new TestNode(new ArrayList<>(), meta, new AtomicReference<>()));
            } else {
                final int childSeparator = s.indexOf('-');
                if (childSeparator > -1) {
                    references.put(s.substring(0, childSeparator), s.substring(childSeparator + 2).split(" "));
                } else {
                    final int redirectSeparator = s.indexOf('+');
                    references.put(s.substring(0, redirectSeparator), new String[]{null, s.substring(redirectSeparator + 2)});
                }
            }
        }
        final ArrayList<TestNode> result = new ArrayList<>();
        List<Runnable> redirectSetters = new ArrayList<>();
        resolveNode(rootId, references, nodes, result, new HashMap<>(), redirectSetters, "");
        redirectSetters.forEach(Runnable::run);
        return result;
    }

    private static String resolveNode(String id, Map<String, String[]> references,
                                   Map<String, TestNode> nodes, ArrayList<TestNode> result,
                                      Map<String, String> nameToMetaPath,
                                   List<Runnable> redirectSetters, String metaPath) {
        final TestNode node = nodes.get(id);
        final String[] refs = references.get(id);
        final String path = metaPath + "#" + node.meta;
        if (refs == null) {
            result.add(node);
            nameToMetaPath.put(id, path);
            return path;
        } else if (refs[0] == null) {
             redirectSetters.add(() -> node.redirect.set(nameToMetaPath.get(refs[1])));
        } else {
            for (String ref : refs) {
                node.children.add(resolveNode(ref, references, nodes, result, nameToMetaPath, redirectSetters, path));
            }
        }
        result.add(node);
        nameToMetaPath.put(id, path);
        return path;
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
