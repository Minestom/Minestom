package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

record GraphImpl(Node root) implements Graph {

    record ConversionNode(Map<Argument<?>, ConversionNode> next) {
    }

    static GraphImpl fromCommand(Command command) {
        ConversionNode root = new ConversionNode(new HashMap<>());
        for (var syntax : command.getSyntaxes()) {
            ConversionNode syntaxNode = root;
            for (int i = 0; i < syntax.getArguments().length; i++) {
                final Argument<?> arg = syntax.getArguments()[i];
                ConversionNode tmp = syntaxNode.next.get(arg);
                if (tmp == null) {
                    tmp = new ConversionNode(new HashMap<>());
                    syntaxNode.next.put(arg, tmp);
                }
                syntaxNode = tmp;
            }
        }

        BuilderImpl builder = new BuilderImpl(Literal(command.getName()));
        for (var test : root.next.entrySet()) recursiveConversion(test, builder);
        return builder.build();
    }

    static void recursiveConversion(Map.Entry<Argument<?>, ConversionNode> entry, Builder builder) {
        builder.append(entry.getKey(), b -> {
            for (var e : entry.getValue().next.entrySet()) recursiveConversion(e, b);
        });
    }

    static GraphImpl merge(Graph... graphs) {
        BuilderImpl builder = new BuilderImpl(Literal(""));
        for (Graph graph : graphs) {
            recursiveMerge(graph.root(), builder);
        }
        return builder.build();
    }

    @Override
    public boolean compare(@NotNull Graph graph, @NotNull Comparator comparator) {
        // We currently do not include execution data in the graph
        return equals(graph);
    }

    static void recursiveMerge(Node node, Builder builder) {
        final List<Node> args = node.next();
        if (args.isEmpty()) {
            builder.append(node.argument());
        } else {
            builder.append(node.argument(), b -> {
                for (var arg : args) recursiveMerge(arg, b);
            });
        }
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
            final Node root = builderToNode(this);
            return new GraphImpl(root);
        }
    }

    static Node builderToNode(BuilderImpl builder) {
        return new NodeImpl(builder.argument, builder.children.stream().map(GraphImpl::builderToNode).toList());
    }

    record NodeImpl(Argument<?> argument, List<Graph.Node> next) implements Graph.Node {
    }
}
