package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

        @UnknownNullability Execution execution();

        @NotNull List<@NotNull Node> next();
    }

    sealed interface Execution extends Predicate<CommandSender> permits GraphImpl.ExecutionImpl {
        // TODO execute the node
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
