package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

sealed interface Graph permits GraphImpl {
    static @NotNull Builder builder(@NotNull Argument<?> argument) {
        return new GraphImpl.BuilderImpl(argument);
    }

    static @NotNull Graph fromCommand(@NotNull Command command) {
        final Graph graph = GraphImpl.fromCommand(command);
        GraphValidator.verifyConflict(graph);
        return graph;
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

        @NotNull List<@NotNull Node> next();
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
