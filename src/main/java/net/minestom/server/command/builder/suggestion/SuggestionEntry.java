package net.minestom.server.command.builder.suggestion;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

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
        if (!(o instanceof SuggestionEntry that)) return false;
        return getEntry().equals(that.getEntry()) && Objects.equals(getTooltip(), that.getTooltip());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntry(), getTooltip());
    }
}
