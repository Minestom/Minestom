package net.minestom.server.command;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;

final class Node {
    private final int id;
    private final IntSet children = new IntOpenHashSet();
    private final IntSet childrenView = IntSets.unmodifiable(children);
    private final DeclareCommandsPacket.NodeType type;
    private String name;
    private Integer redirectTarget;
    private Argument<?> argument;
    private boolean executable;

    Node(int id, DeclareCommandsPacket.NodeType type) {
        this.id = id;
        this.type = type;
    }

    Node(int id) {
        this(id, DeclareCommandsPacket.NodeType.ROOT);
    }

    Node(int id, String name, Integer redirectTarget) {
        this(id, DeclareCommandsPacket.NodeType.LITERAL);
        setName(name);
        setRedirectTarget(redirectTarget);
    }

    Node(int id, Argument<?> argument) {
        this(id, DeclareCommandsPacket.NodeType.ARGUMENT);
        setName(argument.getId());
        this.argument = argument;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRedirectTarget(Integer redirectTarget) {
        this.redirectTarget = redirectTarget;
    }

    public void addChild(Node ...nodes) {
        for (Node node : nodes) {
            children.add(node.id);
        }
    }

    public boolean isParentOf(Node node) {
        return children.contains(node.id());
    }

    public int id() {
        return id;
    }

    public DeclareCommandsPacket.NodeType type() {
        return type;
    }

    public IntSet children() {
        return childrenView;
    }

    public Integer redirectTarget() {
        return redirectTarget;
    }

    public boolean isRoot() {
        return type == DeclareCommandsPacket.NodeType.ROOT;
    }

    public DeclareCommandsPacket.Node getPacketNode() {
        final DeclareCommandsPacket.Node node = new DeclareCommandsPacket.Node();
        node.children = children.toIntArray();
        node.flags = DeclareCommandsPacket.getFlag(type, executable, redirectTarget != null,
                type == DeclareCommandsPacket.NodeType.ARGUMENT && argument.hasSuggestion());
        node.name = name;
        if (redirectTarget != null) {
            node.redirectedNode = redirectTarget;
        }
        if (type == DeclareCommandsPacket.NodeType.ARGUMENT) {
            node.properties = argument.nodeProperties();
            node.parser = argument.parser();
            if (argument.hasSuggestion()) {
                //noinspection ConstantConditions
                node.suggestionsType = argument.suggestionType().getIdentifier();
            }
        }
        return node;
    }
}
