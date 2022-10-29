package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static net.minestom.server.command.builder.arguments.ArgumentType.Word;

record GraphImpl(NodeImpl root) implements Graph {
    static GraphImpl fromCommand(Command command) {
        return new GraphImpl(NodeImpl.command(command));
    }

    static Graph merge(Collection<Command> commands) {
        return new GraphImpl(NodeImpl.rootCommands(commands));
    }

    static GraphImpl merge(List<Graph> graphs) {
        final List<Node> children = graphs.stream().map(Graph::root).toList();
        final NodeImpl root = new NodeImpl(Literal(""), null, children);
        return new GraphImpl(root);
    }

    @Override
    public boolean compare(@NotNull Graph graph, @NotNull Comparator comparator) {
        return compare(root, graph.root(), comparator);
    }

    record BuilderImpl(Argument<?> argument, List<BuilderImpl> children, Execution execution) implements Graph.Builder {
        public BuilderImpl(Argument<?> argument, Execution execution) {
            this(argument, new ArrayList<>(), execution);
        }

        @Override
        public Graph.@NotNull Builder append(@NotNull Argument<?> argument, @Nullable Execution execution,
                                             @NotNull Consumer<Graph.Builder> consumer) {
            BuilderImpl builder = new BuilderImpl(argument, execution);
            consumer.accept(builder);
            this.children.add(builder);
            return this;
        }

        @Override
        public Graph.@NotNull Builder append(@NotNull Argument<?> argument, @Nullable Execution execution) {
            this.children.add(new BuilderImpl(argument, List.of(), execution));
            return this;
        }

        @Override
        public @NotNull GraphImpl build() {
            return new GraphImpl(NodeImpl.fromBuilder(this));
        }
    }

    record NodeImpl(Argument<?> argument, ExecutionImpl execution, List<Graph.Node> next) implements Graph.Node {
        static NodeImpl fromBuilder(BuilderImpl builder) {
            final List<BuilderImpl> children = builder.children;
            Node[] nodes = new NodeImpl[children.size()];
            for (int i = 0; i < children.size(); i++) nodes[i] = fromBuilder(children.get(i));
            return new NodeImpl(builder.argument, (ExecutionImpl) builder.execution, List.of(nodes));
        }

        static NodeImpl command(Command command) {
            return ConversionNode.fromCommand(command).toNode();
        }

        static NodeImpl rootCommands(Collection<Command> commands) {
            return ConversionNode.rootConv(commands).toNode();
        }
    }

    record ExecutionImpl(Predicate<CommandSender> predicate,
                         CommandExecutor defaultExecutor, CommandExecutor globalListener,
                         CommandExecutor executor, CommandCondition condition) implements Execution {
        @Override
        public boolean test(CommandSender commandSender) {
            return predicate.test(commandSender);
        }

        static ExecutionImpl fromCommand(Command command) {
            final CommandExecutor defaultExecutor = command.getDefaultExecutor();
            final CommandCondition defaultCondition = command.getCondition();

            CommandExecutor executor = defaultExecutor;
            CommandCondition condition = defaultCondition;
            for (var syntax : command.getSyntaxes()) {
                if (syntax.getArguments().length == 0) {
                    executor = syntax.getExecutor();
                    condition = syntax.getCommandCondition();
                    break;
                }
            }
            final CommandExecutor globalListener = (sender, context) -> command.globalListener(sender, context, context.getInput());

            return new ExecutionImpl(commandSender -> defaultCondition == null || defaultCondition.canUse(commandSender, null),
                    defaultExecutor, globalListener, executor, condition);
        }

        static ExecutionImpl fromSyntax(CommandSyntax syntax) {
            final CommandExecutor executor = syntax.getExecutor();
            final CommandCondition condition = syntax.getCommandCondition();
            return new ExecutionImpl(commandSender -> condition == null || condition.canUse(commandSender, null),
                    null, null, executor, condition);
        }
    }

    private record ConversionNode(Argument<?> argument, ExecutionImpl execution,
                                  Map<Argument<?>, ConversionNode> nextMap) {
        ConversionNode(Argument<?> argument, ExecutionImpl execution) {
            this(argument, execution, new LinkedHashMap<>());
        }

        private NodeImpl toNode() {
            Node[] nodes = new NodeImpl[nextMap.size()];
            int i = 0;
            for (var entry : nextMap.values()) nodes[i++] = entry.toNode();
            return new NodeImpl(argument, execution, List.of(nodes));
        }

        static ConversionNode fromCommand(Command command) {
            ConversionNode root = new ConversionNode(commandToArgument(command), ExecutionImpl.fromCommand(command));
            // Syntaxes
            for (CommandSyntax syntax : command.getSyntaxes()) {
                ConversionNode syntaxNode = root;
                for (Argument<?> arg : syntax.getArguments()) {
                    boolean last = arg == syntax.getArguments()[syntax.getArguments().length - 1];
                    syntaxNode = syntaxNode.nextMap.computeIfAbsent(arg, argument -> {
                        var ex = last ? ExecutionImpl.fromSyntax(syntax) : null;
                        return new ConversionNode(argument, ex);
                    });
                }
            }
            // Subcommands
            for (Command subcommand : command.getSubcommands()) {
                root.nextMap.put(commandToArgument(subcommand), fromCommand(subcommand));
            }
            return root;
        }

        static ConversionNode rootConv(Collection<Command> commands) {
            Map<Argument<?>, ConversionNode> next = new LinkedHashMap<>(commands.size());
            for (Command command : commands) {
                final ConversionNode conv = fromCommand(command);
                next.put(conv.argument, conv);
            }
            return new ConversionNode(Literal(""), null, next);
        }
    }

    static Argument<String> commandToArgument(Command command) {
        final String[] aliases = command.getNames();
        if (aliases.length == 1) return Literal(aliases[0]);
        return Word(command.getName()).from(command.getNames());
    }

    static boolean compare(@NotNull Node first, Node second, @NotNull Comparator comparator) {
        return switch (comparator) {
            case TREE -> {
                if (!first.argument().equals(second.argument())) yield false;
                if (first.next().size() != second.next().size()) yield false;
                for (int i = 0; i < first.next().size(); i++) {
                    if (!compare(first.next().get(i), second.next().get(i), comparator)) {
                        yield false;
                    }
                }
                yield true;
            }
        };
    }
}
