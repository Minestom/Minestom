package net.minestom.server.command.builder.arguments.minecraft;

import org.jetbrains.annotations.NotNull;

public enum SuggestionType {

    ASK_SERVER("minecraft:ask_server"),
    ALL_RECIPES("minecraft:all_recipes"),
    AVAILABLE_SOUNDS("minecraft:available_sounds"),
    SUMMONABLE_ENTITIES("minecraft:summonable_entities");

    private final String identifier;

    SuggestionType(@NotNull String identifier) {
        this.identifier = identifier;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }
}
