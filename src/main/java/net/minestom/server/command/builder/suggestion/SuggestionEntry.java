package net.minestom.server.command.builder.suggestion;

import net.minestom.server.chat.JsonMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuggestionEntry {

    private final String entry;
    private final JsonMessage tooltip;

    public SuggestionEntry(@NotNull String entry, @Nullable JsonMessage tooltip) {
        this.entry = entry;
        this.tooltip = tooltip;
    }

    public SuggestionEntry(@NotNull String entry) {
        this(entry, null);
    }

    @NotNull
    public String getEntry() {
        return entry;
    }

    @Nullable
    public JsonMessage getTooltip() {
        return tooltip;
    }
}
