package net.minestom.server.command;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

final class GraphBuilder {
    private final AtomicInteger idSource = new AtomicInteger();
    private final ObjectSet<Node> nodes = new ObjectOpenHashSet<>();
    private final ObjectSet<Supplier<Boolean>> redirectWaitList = new ObjectOpenHashSet<>();
    private final Node root = rootNode();

    private Node rootNode() {
        final Node rootNode = new Node(idSource.getAndIncrement());
        nodes.add(rootNode);
        return rootNode;
    }

    public Node createLiteralNode(String name, boolean addToRoot, boolean executable, @Nullable String[] aliases, @Nullable Integer redirectTo) {
        if (aliases != null) {
            final Node node = createLiteralNode(name, addToRoot, executable, null, null);
            for (String alias : aliases) {
                createLiteralNode(alias, addToRoot, false, null, node.getId());
            }
            return node;
        } else {
            final Node literalNode = new Node(idSource.getAndIncrement(), name, redirectTo);
            literalNode.setExecutable(executable);
            nodes.add(literalNode);
            if (addToRoot) root.addChild(literalNode);
            return literalNode;
        }
    }

    public Node[] createArgumentNode(Argument<?> argument, boolean executable) {
        final Node[] nodes;
        Integer overrideRedirectTarget = null;
        if (argument instanceof ArgumentEnum<?> argumentEnum) {
            nodes = argumentEnum.entries().stream().map(x -> createLiteralNode(x, false, executable, null, null)).toArray(Node[]::new);
        } else if (argument instanceof ArgumentGroup argumentGroup) {
            nodes = argumentGroup.group().stream().map(x -> createArgumentNode(x, executable)).flatMap(Stream::of).toArray(Node[]::new);
        } else if (argument instanceof ArgumentLoop<?> argumentLoop) {
            overrideRedirectTarget = idSource.get()-1;
            nodes = argumentLoop.arguments().stream().map(x -> createArgumentNode(x, executable)).flatMap(Stream::of).toArray(Node[]::new);
        } else {
            if (argument instanceof ArgumentCommand) {
                return new Node[]{createLiteralNode(argument.getId(), false, false, null, 0)};
            }
            final int id = idSource.getAndIncrement();
            nodes = new Node[] {argument instanceof ArgumentLiteral ? new Node(id, argument.getId(), null) : new Node(id, argument)};
        }
        for (Node node : nodes) {
            node.setExecutable(executable);
            this.nodes.add(node);
            Integer finalOverrideRedirectTarget = overrideRedirectTarget;
            if (finalOverrideRedirectTarget != null) {
                redirectWaitList.add(() -> {
                    int target = finalOverrideRedirectTarget;
                    if (target != -1) {
                        node.setRedirectTarget(target);
                        return true;
                    }
                    return false;
                });
            }
        }
        return nodes;
    }

    private int tryResolveId(String[] path) {
        if (path.length == 0) {
            return root.getId();
        } else {
            Node target = root;
            for (String next : path) {
                Node finalTarget = target;
                final Optional<Node> result = nodes.stream().filter(finalTarget::isParentOf)
                        .filter(x -> x.name().equals(next)).findFirst();
                if (result.isEmpty()) {
                    return -1;
                } else {
                    target = result.get();
                }
            }
            return target.getId();
        }
    }

    private void finalizeStructure() {
        redirectWaitList.removeIf(Supplier::get);
        if (redirectWaitList.size() > 0)
            throw new RuntimeException("Could not set redirects for all arguments! Did you provide a correct id path which doesn't rely on redirects?");
    }

    public DeclareCommandsPacket createCommandPacket() {
        finalizeStructure();
        return new DeclareCommandsPacket(nodes.stream().sorted(Comparator.comparingInt(Node::getId))
                .map(Node::getPacketNode).toList(), root.getId());
    }
}
