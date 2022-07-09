package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

record GraphImpl(Node root) implements Graph {
    static GraphImpl fromCommand(Command command) {
        final ConversionNode conv = ConversionNode.fromCommand(command);
        final Node root = NodeImpl.fromConversionNode(Literal(command.getName()), conv);
        return new GraphImpl(root);
    }

    static Graph merge(Collection<Command> commands) {
        final ConversionNode conv = new ConversionNode();
        for (var command : commands) {
            conv.next.put(Literal(command.getName()), ConversionNode.fromCommand(command));
        }
        final Node root = NodeImpl.fromConversionNode(Literal(""), conv);
        return new GraphImpl(root);
    }

    static GraphImpl merge(List<Graph> graphs) {
        final List<Node> children = graphs.stream().map(Graph::root).toList();
        final Node root = new NodeImpl(Literal(""), children);
        return new GraphImpl(root);
    }

    @Override
    public boolean compare(@NotNull Graph graph, @NotNull Comparator comparator) {
        // We currently do not include execution data in the graph
        return equals(graph);
    }

    static final class BuilderImpl implements Graph.Builder {
        private final Argument<?> argument;
        private final List<BuilderImpl> children;

        public BuilderImpl(Argument<?> argument, List<BuilderImpl> children) {
            this.argument = argument;
            this.children = children;
        }

        public BuilderImpl(Argument<?> argument) {
            this(argument, new ArrayList<>());
        }

        @Override
        public Graph.@NotNull Builder append(@NotNull Argument<?> argument, @NotNull Consumer<Graph.Builder> consumer) {
            BuilderImpl builder = new BuilderImpl(argument);
            consumer.accept(builder);
            this.children.add(builder);
            return this;
        }

        @Override
        public Graph.@NotNull Builder append(@NotNull Argument<?> argument) {
            this.children.add(new BuilderImpl(argument, List.of()));
            return this;
        }

        @Override
        public @NotNull GraphImpl build() {
            final Node root = NodeImpl.fromBuilder(this);
            return new GraphImpl(root);
        }
    }

    record NodeImpl(Argument<?> argument, List<Graph.Node> next) implements Graph.Node {
        static NodeImpl fromBuilder(BuilderImpl builder) {
            final List<BuilderImpl> children = builder.children;
            Node[] nodes = new NodeImpl[children.size()];
            for (int i = 0; i < children.size(); i++) nodes[i] = fromBuilder(children.get(i));
            return new NodeImpl(builder.argument, List.of(nodes));
        }

        static Node fromConversionNode(Argument<?> argument, ConversionNode conv) {
            final Map<Argument<?>, ConversionNode> next = conv.next;
            Node[] nodes = new NodeImpl[next.size()];
            int i = 0;
            for (var entry : next.entrySet()) nodes[i++] = fromConversionNode(entry.getKey(), entry.getValue());
            return new NodeImpl(argument, List.of(nodes));
        }
    }

    record ConversionNode(Map<Argument<?>, ConversionNode> next) {
        ConversionNode() {
            this(new LinkedHashMap<>());
        }

        static ConversionNode fromCommand(Command command) {
            ConversionNode root = new ConversionNode();
            for (var syntax : command.getSyntaxes()) {
                ConversionNode syntaxNode = root;
                for (Argument<?> arg : syntax.getArguments()) {
                    ConversionNode tmp = syntaxNode.next.get(arg);
                    if (tmp == null) {
                        tmp = new ConversionNode();
                        syntaxNode.next.put(arg, tmp);
                    }
                    syntaxNode = tmp;
                }
            }
            return root;
        }
    }
}
