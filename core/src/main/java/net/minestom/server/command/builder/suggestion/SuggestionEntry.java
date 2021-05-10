package net.minestom.server.command.builder.suggestion;

import net.kyori.adventure.text.Component;
import net.minestom.server.chat.JsonMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuggestionEntry {

    private final String entry;
    private final Component tooltip;

    /**
     * @deprecated Use {{@link #SuggestionEntry(String, JsonMessage)}}
     */
    @Deprecated
    public SuggestionEntry(@NotNull String entry, @Nullable JsonMessage tooltip) {
        this.entry = entry;
        this.tooltip = tooltip.asComponent();
    }

    public SuggestionEntry(@NotNull String entry, @Nullable Component tooltip) {
        this.entry = entry;
        this.tooltip = tooltip.asComponent();
    }

    public SuggestionEntry(@NotNull String entry) {
        this.entry = entry;
        this.tooltip = null;
    }

    @NotNull
    public String getEntry() {
        return entry;
    }

    @Nullable
    public Component getTooltip() {
        return tooltip;
    }

    /**
     * @deprecated Use {@link #getTooltip()}
     */
    @Deprecated
    @Nullable
    public JsonMessage getTooltipJson() {
        return JsonMessage.fromComponent(tooltip);
    }
}
