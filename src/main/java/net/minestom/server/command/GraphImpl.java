package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

record GraphImpl(NodeImpl root) implements Graph {
    static GraphImpl fromCommand(Command command) {
        return new GraphImpl(NodeImpl.command(command));
    }

    static Graph merge(Collection<Command> commands) {
        return new GraphImpl(NodeImpl.rootCommands(commands));
    }

    static GraphImpl merge(List<Graph> graphs) {
        final List<Node> children = graphs.stream().map(Graph::root).toList();
        final NodeImpl root = new NodeImpl(Literal(""), children);
        return new GraphImpl(root);
    }

    @Override
    public boolean compare(@NotNull Graph graph, @NotNull Comparator comparator) {
        // We currently do not include execution data in the graph
        return equals(graph);
    }

    record BuilderImpl(Argument<?> argument, List<BuilderImpl> children) implements Graph.Builder {
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
            return new GraphImpl(NodeImpl.fromBuilder(this));
        }
    }

    record NodeImpl(Argument<?> argument, List<Graph.Node> next) implements Graph.Node {
        static NodeImpl fromBuilder(BuilderImpl builder) {
            final List<BuilderImpl> children = builder.children;
            Node[] nodes = new NodeImpl[children.size()];
            for (int i = 0; i < children.size(); i++) nodes[i] = fromBuilder(children.get(i));
            return new NodeImpl(builder.argument, List.of(nodes));
        }

        static NodeImpl command(Command command) {
            return ConversionNode.fromCommand(command).toNode();
        }

        static NodeImpl rootCommands(Collection<Command> commands) {
            return ConversionNode.rootConv(commands).toNode();
        }
    }

    private record ConversionNode(Argument<?> argument, Map<Argument<?>, ConversionNode> nextMap) {
        ConversionNode(Argument<?> argument) {
            this(argument, new LinkedHashMap<>());
        }

        private NodeImpl toNode() {
            Node[] nodes = new NodeImpl[nextMap.size()];
            int i = 0;
            for (var entry : nextMap.values()) nodes[i++] = entry.toNode();
            return new NodeImpl(argument, List.of(nodes));
        }

        static ConversionNode fromCommand(Command command) {
            ConversionNode root = new ConversionNode(Literal(command.getName()));
            for (var syntax : command.getSyntaxes()) {
                ConversionNode syntaxNode = root;
                for (Argument<?> arg : syntax.getArguments()) {
                    syntaxNode = syntaxNode.nextMap.computeIfAbsent(arg, ConversionNode::new);
                }
            }
            return root;
        }

        static ConversionNode rootConv(Collection<Command> commands) {
            Map<Argument<?>, ConversionNode> next = new LinkedHashMap<>(commands.size());
            for (Command command : commands) {
                final ConversionNode conv = fromCommand(command);
                next.put(conv.argument, conv);
            }
            return new ConversionNode(Literal(""), next);
        }
    }
}
