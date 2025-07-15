package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

sealed interface Graph permits GraphImpl {
    static Builder builder(Argument<?> argument, @Nullable Execution execution) {
        return new GraphImpl.BuilderImpl(argument, execution);
    }

    static Builder builder(Argument<?> argument) {
        return new GraphImpl.BuilderImpl(argument, null);
    }

    static Graph fromCommand(Command command) {
        return GraphImpl.fromCommand(command);
    }

    static Graph merge(Collection<Command> commands) {
        return GraphImpl.merge(commands);
    }

    static Graph merge(List<Graph> graphs) {
        return GraphImpl.merge(graphs);
    }

    static Graph merge(Graph ... graphs) {
        return merge(List.of(graphs));
    }

    Node root();

    boolean compare(Graph graph, Comparator comparator);

    sealed interface Node permits GraphImpl.NodeImpl {
        Argument<?> argument();

        @UnknownNullability Execution execution();

        List<Node> next();
    }

    sealed interface Execution extends Predicate<CommandSender> permits GraphImpl.ExecutionImpl {
        @UnknownNullability CommandExecutor defaultExecutor();

        @UnknownNullability CommandExecutor globalListener();

        /**
         * Non-null if the command at this point considered executable, must be present
         * on the last node of the syntax.
         */
        @Nullable CommandExecutor executor();

        /**
         * Non-null if the command or syntax has a condition, must be present
         * only on nodes that specify it
         */
        @Nullable CommandCondition condition();
    }

    sealed interface Builder permits GraphImpl.BuilderImpl {
        Builder append(Argument<?> argument, @Nullable Execution execution, Consumer<Builder> consumer);

        Builder append(Argument<?> argument, @Nullable Execution execution);

        default Builder append(Argument<?> argument, Consumer<Builder> consumer) {
            return append(argument, null, consumer);
        }

        default Builder append(Argument<?> argument) {
            return append(argument, (Execution) null);
        }

        Graph build();
    }

    enum Comparator {
        TREE
    }
}
