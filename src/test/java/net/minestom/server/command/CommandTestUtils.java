package net.minestom.server.command;

import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;
import org.opentest4j.AssertionFailedError;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CommandTestUtils {

    public static void assertPacket(DeclareCommandsPacket packet, String expectedStructure) {
        final List<NodeStructure.TestNode> expectedList = NodeStructure.fromString("0\n0=$root$\n" + expectedStructure);
        final List<NodeStructure.TestNode> actualList = NodeStructure.fromString(NodeStructure.packetToString(packet));
        try {
            assertEquals(expectedList.size(), actualList.size(), "Different node counts");
            for (NodeStructure.TestNode expected : expectedList) {
                boolean found = false;
                for (NodeStructure.TestNode actual : actualList) {
                    if (expected.equals(actual)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fail("Packet doesn't contain " + expected.toString());
                }
            }
        } catch (AssertionFailedError error) {
            fail("Graphs didn't match. Actual graph from packet: " + CommandTestUtils.exportGarphvizDot(packet, false), error);
        }
    }

    static class NodeStructure {
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

        static String packetToString(DeclareCommandsPacket packet) {
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

        static List<TestNode> fromString(String input) {
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

        record TestNode(List<String> children, String meta, AtomicReference<String> redirect) {
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
    }

    static String exportGarphvizDot(DeclareCommandsPacket packet, boolean prettyPrint) {
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

}
