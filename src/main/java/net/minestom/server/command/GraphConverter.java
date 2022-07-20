package net.minestom.server.command;

import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

final class GraphConverter {
    private GraphConverter() {
        //no instance
    }

    @Contract("_, _ -> new")
    public static DeclareCommandsPacket createPacket(Graph graph, @Nullable Player player) {
        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();
        List<Consumer<Integer>> rootRedirect = new ArrayList<>();
        final AtomicInteger idSource = new AtomicInteger(0);
        final int rootId = append(graph.root(), nodes, rootRedirect, idSource, null, null, player)[0];
        for (var i : rootRedirect) {
            i.accept(rootId);
        }
        return new DeclareCommandsPacket(nodes, rootId);
    }

    private static int[] append(Graph.Node graphNode, List<DeclareCommandsPacket.Node> to,
                                List<Consumer<Integer>> rootRedirect, AtomicInteger id, @Nullable AtomicInteger redirect,
                                List<Runnable> redirectSetters, @Nullable Player player) {
        final Graph.Execution execution = graphNode.execution();
        if (player != null && execution != null) {
            if (!execution.test(player)) return new int[0];
        }

        final Argument<?> argument = graphNode.argument();
        final List<Graph.Node> children = graphNode.next();

        final DeclareCommandsPacket.Node node = new DeclareCommandsPacket.Node();
        int[] packetNodeChildren = new int[children.size()];
        for (int i = 0, appendIndex = 0; i < children.size(); i++) {
            final int[] append = append(children.get(i), to, rootRedirect, id, redirect, redirectSetters, player);
            if (append.length == 1) {
                packetNodeChildren[appendIndex++] = append[0];
            } else {
                packetNodeChildren = Arrays.copyOf(packetNodeChildren, packetNodeChildren.length + append.length - 1);
                System.arraycopy(append, 0, packetNodeChildren, appendIndex, append.length);
                appendIndex += append.length;
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
                    redirectSetters.add(() -> node.redirectedNode = redirect.get());
                }
            }
            to.add(node);
            return new int[]{id.getAndIncrement()};
        } else {
            if (argument instanceof ArgumentCommand) {
                node.flags = literal(false, true);
                node.name = argument.getId();
                rootRedirect.add(i -> node.redirectedNode = i);
                to.add(node);

                return new int[]{id.getAndIncrement()};
            } else if (argument instanceof ArgumentEnum<?> || (argument instanceof ArgumentWord word && word.hasRestrictions())) {
                List<String> entries = argument instanceof ArgumentEnum<?> ? ((ArgumentEnum<?>) argument).entries() : Arrays.stream(((ArgumentWord) argument).getRestrictions()).toList();
                final int[] res = new int[entries.size()];
                for (int i = 0; i < res.length; i++) {
                    String entry = entries.get(i);
                    final DeclareCommandsPacket.Node subNode = new DeclareCommandsPacket.Node();
                    subNode.children = node.children;
                    subNode.flags = literal(false, false);
                    subNode.name = entry;
                    if (redirect != null) {
                        subNode.flags |= 0x8;
                        redirectSetters.add(() -> subNode.redirectedNode = redirect.get());
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
                    if (i == entries.size() - 1) {
                        // Last will be the parent of next args
                        final int[] l = append(new GraphImpl.NodeImpl(entry, null, List.of()), to, rootRedirect, id, redirect, redirectSetters, player);
                        for (int n : l) {
                            to.get(n).children = node.children;
                        }
                        for (int n : last) {
                            to.get(n).children = l;
                        }
                        return res == null ? l : res;
                    } else if (i == 0) {
                        // First will be the children & parent of following
                        res = append(new GraphImpl.NodeImpl(entry, null, List.of()), to, rootRedirect, id, null, redirectSetters, player);
                        last = res;
                    } else {
                        final int[] l = append(new GraphImpl.NodeImpl(entry, null, List.of()), to, rootRedirect, id, null, redirectSetters, player);
                        for (int n : last) {
                            to.get(n).children = l;
                        }
                        last = l;
                    }
                }
                throw new RuntimeException("Arg group must have child args.");
            } else if (argument instanceof ArgumentLoop special) {
                AtomicInteger r = new AtomicInteger();
                List<Runnable> setters = new ArrayList<>();
                int[] res = new int[special.arguments().size()];
                List<?> arguments = special.arguments();
                for (int i = 0, appendIndex = 0; i < arguments.size(); i++) {
                    Object arg = arguments.get(i);
                    final int[] append = append(new GraphImpl.NodeImpl((Argument<?>) arg, null, List.of()), to, rootRedirect, id, r, setters, player);
                    if (append.length == 1) {
                        res[appendIndex++] = append[0];
                    } else {
                        res = Arrays.copyOf(res, res.length + append.length - 1);
                        System.arraycopy(append, 0, res, appendIndex, append.length);
                        appendIndex += append.length;
                    }
                }
                r.set(id.get());
                setters.forEach(Runnable::run);
                return res;
            } else {
                node.flags = arg(false, argument.hasSuggestion());
                node.name = argument.getId();
                node.parser = argument.parser();
                node.properties = argument.nodeProperties();
                if (redirect != null) {
                    node.flags |= 0x8;
                    redirectSetters.add(() -> node.redirectedNode = redirect.get());
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
