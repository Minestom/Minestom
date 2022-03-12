package net.minestom.server.command.builder.suggestion;

import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SuggestionCallback {
    void apply(@NotNull CommandOrigin origin, @NotNull CommandContext context, @NotNull Suggestion suggestion);
}
