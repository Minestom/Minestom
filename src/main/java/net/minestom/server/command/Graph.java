package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

interface Graph {
    static @NotNull Builder builder(@NotNull Argument<?> argument) {
        return new GraphImpl.BuilderImpl(argument);
    }

    static @NotNull Graph fromCommand(@NotNull Command command) {
        return GraphImpl.fromCommand(command);
    }

    @NotNull Node root();

    interface Node {
        @NotNull Argument<?> argument();

        @NotNull List<@NotNull Node> next();
    }

    interface Builder {
        @NotNull Builder append(@NotNull Argument<?> argument, @NotNull Consumer<Builder> consumer);

        @NotNull Builder append(@NotNull Argument<?> argument);

        @NotNull Graph build();
    }
}
