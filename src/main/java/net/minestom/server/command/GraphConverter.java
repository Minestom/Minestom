package net.minestom.server.command;

import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class GraphConverter {
    private GraphConverter() {
        //no instance
    }

    @Contract("_ -> new")
    public static DeclareCommandsPacket createPacket(Graph graph) {
        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();
        List<DeclareCommandsPacket.Node> rootRedirect = new ArrayList<>();
        final AtomicInteger idSource = new AtomicInteger(0);
        final int rootId = append(graph.root(), nodes, rootRedirect, idSource, null)[0];
        for (DeclareCommandsPacket.Node node : rootRedirect) {
            node.redirectedNode = rootId;
        }
        return new DeclareCommandsPacket(nodes, rootId);
    }

    private static int[] append(Graph.Node graphNode, List<DeclareCommandsPacket.Node> to,
                                List<DeclareCommandsPacket.Node> rootRedirect, AtomicInteger id, @Nullable Integer redirect) {
        final Argument<?> argument = graphNode.argument();
        final List<Graph.Node> children = graphNode.next();

        final DeclareCommandsPacket.Node node = new DeclareCommandsPacket.Node();
        int[] packetNodeChildren = new int[children.size()];
        for (int i = 0; i < packetNodeChildren.length; i++) {
            final int[] append = append(children.get(i), to, rootRedirect, id, redirect);
            if (append.length == 1) {
                packetNodeChildren[i] = append[0];
            } else {
                packetNodeChildren = Arrays.copyOf(packetNodeChildren, packetNodeChildren.length+append.length-1);
                System.arraycopy(append, 0, packetNodeChildren, i, append.length);
                i += append.length;
            }
        }
        node.children = packetNodeChildren;
        if (argument instanceof ArgumentLiteral literal) {
            if (literal.getId().isEmpty()) {
                node.flags = 0; //root
            } else {
                node.flags = literal(false, false);
                node.name = argument.getId();
                if (redirect != null) {
                    node.flags |= 0x8;
                    node.redirectedNode = redirect;
                }
            }
            to.add(node);
            return new int[]{id.getAndIncrement()};
        } else {
            if (argument instanceof ArgumentCommand) {
                node.flags = literal(false, true);
                node.name = argument.getId();
                rootRedirect.add(node);
                to.add(node);

                return new int[]{id.getAndIncrement()};
            } else if (argument instanceof ArgumentEnum<?> special) {
                List<String> entries = special.entries();
                final int[] res = new int[entries.size()];
                for (int i = 0; i < res.length; i++) {
                    String entry = entries.get(i);
                    final DeclareCommandsPacket.Node subNode = new DeclareCommandsPacket.Node();
                    subNode.children = node.children;
                    subNode.flags = literal(false, false);
                    subNode.name = entry;
                    if (redirect != null) {
                        subNode.flags |= 0x8;
                        subNode.redirectedNode = redirect;
                    }
                    to.add(subNode);
                    res[i] = id.getAndIncrement();
                }
                return res;
            } else if (argument instanceof ArgumentGroup special) {
                List<Argument<?>> entries = special.group();
                int[] res = null;
                int[] last = new int[0];
                for (int i = 0; i < entries.size(); i++) {
                    Argument<?> entry = entries.get(i);
                    if (i == entries.size()-1) {
                        // Last will be the parent of next args
                        final int[] l = append(Graph.Node.fromArgument(entry), to, rootRedirect, id, redirect);
                        for (int n : l) {
                            to.get(n).children = node.children;
                        }
                        for (int n : last) {
                            to.get(n).children = l;
                        }
                        return res == null ? l : res;
                    } else if (i == 0) {
                        // First will be the children & parent of following
                        res = append(Graph.Node.fromArgument(entry), to, rootRedirect, id, null);
                        last = res;
                    } else {
                        final int[] l = append(Graph.Node.fromArgument(entry), to, rootRedirect, id, null);
                        for (int n : last) {
                            to.get(n).children = l;
                        }
                        last = l;
                    }
                }
                throw new RuntimeException("Arg group must have child args.");
            } else if (argument instanceof ArgumentLoop special) {
                int r = id.get();
                int[] res = new int[special.arguments().size()];
                List<?> arguments = special.arguments();
                for (int i = 0; i < arguments.size(); i++) {
                    Object arg = arguments.get(i);
                    final int[] append = append(Graph.Node.fromArgument((Argument<?>) arg), to, rootRedirect, id, r);
                    if (append.length == 1) {
                        res[i] = append[0];
                    } else {
                        res = Arrays.copyOf(res, res.length+append.length-1);
                        System.arraycopy(append, 0, res, i, append.length);
                        i += append.length;
                    }
                }
                return res;
            } else {
                node.flags = arg(false, argument.hasSuggestion());
                node.name = argument.getId();
                node.parser = argument.parser();
                node.properties = argument.nodeProperties();
                if (redirect != null) {
                    node.flags |= 0x8;
                    node.redirectedNode = redirect;
                }
                to.add(node);
                return new int[]{id.getAndIncrement()};
            }
        }
    }

    private static byte literal(boolean executable, boolean hasRedirect) {
        return DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL, executable, hasRedirect, false);
    }

    private static byte arg(boolean executable, boolean hasSuggestion) {
        return DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.ARGUMENT, executable, false, hasSuggestion);
    }
}
