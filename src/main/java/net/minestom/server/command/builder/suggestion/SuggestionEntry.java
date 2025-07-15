package net.minestom.server.command.builder.suggestion;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class SuggestionEntry {
    private final String entry;
    private final Component tooltip;

    public SuggestionEntry(String entry, @Nullable Component tooltip) {
        this.entry = entry;
        this.tooltip = tooltip;
    }

    public SuggestionEntry(String entry) {
        this(entry, null);
    }

    public String getEntry() {
        return entry;
    }

    public @Nullable Component getTooltip() {
        return tooltip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuggestionEntry that = (SuggestionEntry) o;
        return Objects.equals(entry, that.entry) && Objects.equals(tooltip, that.tooltip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entry, tooltip);
    }
}
