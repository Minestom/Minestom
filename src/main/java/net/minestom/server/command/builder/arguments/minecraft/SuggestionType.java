package net.minestom.server.command.builder.arguments.minecraft;

import org.jetbrains.annotations.NotNull;

public enum SuggestionType {

    /**
     * Tells the client to ask the server for the tab completions for an argument
     */
    ASK_SERVER("minecraft:ask_server"),

    /**
     * Tells the client to use the NamespaceIDs of registered recipes as the tab completions for an argument
     */
    ALL_RECIPES("minecraft:all_recipes"),

    /**
     * Tells the client to use the names of available sounds as the tab completions for an argument
     */
    AVAILABLE_SOUNDS("minecraft:available_sounds"),

    /**
     * Tells the client to use the NamespaceIDs of registered biomes for the tab completions for an argument
     */
    AVAILABLE_BIOMES("minecraft:available_biomes"),

    /**
     * Tells the client to use summonable entities as the tab completions for an argument
     */
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
