package net.minestom.server.command.builder.suggestion;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuggestionEntry {
    private final String entry;
    private final Component tooltip;

    public SuggestionEntry(@NotNull String entry, @Nullable Component tooltip) {
        this.entry = entry;
        this.tooltip = tooltip;
    }

    public SuggestionEntry(@NotNull String entry) {
        this(entry, null);
    }

    public @NotNull String getEntry() {
        return entry;
    }

    public @Nullable Component getTooltip() {
        return tooltip;
    }
}
