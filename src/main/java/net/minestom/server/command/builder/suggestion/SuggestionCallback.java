package net.minestom.server.command.builder.suggestion;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SuggestionCallback {
    void apply(@NotNull Suggestion suggestion, @NotNull String input);
}
