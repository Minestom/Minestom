package net.minestom.server.command.builder.suggestion;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SuggestionEntry(@NotNull String entry, @Nullable Component tooltip) {

    public SuggestionEntry(@NotNull String entry) {
        this(entry, null);
    }

}
