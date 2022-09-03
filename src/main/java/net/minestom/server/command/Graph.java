package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

sealed interface Graph permits GraphImpl {
    static @NotNull Builder builder(@NotNull Argument<?> argument, @Nullable Execution execution) {
        return new GraphImpl.BuilderImpl(argument, execution);
    }

    static @NotNull Builder builder(@NotNull Argument<?> argument) {
        return new GraphImpl.BuilderImpl(argument, null);
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

        @UnknownNullability Execution execution();

        @NotNull List<@NotNull Node> next();
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
        @NotNull Builder append(@NotNull Argument<?> argument, @Nullable Execution execution, @NotNull Consumer<Builder> consumer);

        @NotNull Builder append(@NotNull Argument<?> argument, @Nullable Execution execution);

        default @NotNull Builder append(@NotNull Argument<?> argument, @NotNull Consumer<Builder> consumer) {
            return append(argument, null, consumer);
        }

        default @NotNull Builder append(@NotNull Argument<?> argument) {
            return append(argument, (Execution) null);
        }

        @NotNull Graph build();
    }

    enum Comparator {
        TREE
    }
}
