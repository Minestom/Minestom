package net.minestom.server.command;

import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

sealed interface Graph permits GraphImpl {
    static @NotNull Builder builder(@NotNull Argument<?> argument) {
        return new GraphImpl.BuilderImpl(argument);
    }

    static @NotNull Graph fromCommand(@NotNull Command command) {
        return GraphImpl.fromCommand(command);
    }

    static @NotNull Graph merge(@NotNull Collection<@NotNull Command> commands) {
        return GraphImpl.merge(commands);
    }

    static @NotNull Graph merge(@NotNull List<@NotNull Graph> graphs) {
        return GraphImpl.merge(graphs);
    }

    static @NotNull Graph merge(@NotNull Graph @NotNull ... graphs) {
        return merge(List.of(graphs));
    }

    @NotNull Node root();

    boolean compare(@NotNull Graph graph, @NotNull Comparator comparator);

    sealed interface Node permits GraphImpl.NodeImpl {
        @NotNull Argument<?> argument();

        @UnknownNullability Executor executor();

        @NotNull List<@NotNull Node> next();
    }

    // TODO rename to ExecutionInformation or similar to avoid confusion
    sealed interface Executor extends Predicate<CommandSender> permits GraphImpl.ExecutorImpl {
        /**
         * Non-null if the command has a default syntax error handler, must be present on
         * declaring node and all subsequent ones, a sub command must continue with its own
         * if present, otherwise the previous has to be propagated further.
         */
        @Nullable ArgumentCallback syntaxErrorCallback();

        /**
         * Non-null if the command at this point considered executable, must be present
         * on last required node and all subsequent optional nodes
         */
        @Nullable CommandExecutor executor();

        /**
         * Non-null if the command or syntax has a condition, must be present
         * only on nodes that specify it
         */
        @Nullable CommandCondition condition();

        /**
         * Non-null if the node at this point considered executable and optional nodes are
         * present after this node, this map must only contain suppliers for following nodes
         */
        @Nullable Map<String, Supplier<?>> defaultValueSuppliers();
    }

    sealed interface Builder permits GraphImpl.BuilderImpl {
        @NotNull Builder append(@NotNull Argument<?> argument, @NotNull Consumer<Builder> consumer);

        @NotNull Builder append(@NotNull Argument<?> argument);

        @NotNull Graph build();
    }

    enum Comparator {
        TREE
    }
}
