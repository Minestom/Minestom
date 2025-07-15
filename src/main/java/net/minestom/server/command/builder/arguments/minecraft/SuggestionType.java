package net.minestom.server.command.builder.arguments.minecraft;


public enum SuggestionType {

    ASK_SERVER("minecraft:ask_server"),
    ALL_RECIPES("minecraft:all_recipes"),
    AVAILABLE_SOUNDS("minecraft:available_sounds"),
    SUMMONABLE_ENTITIES("minecraft:summonable_entities");

    private final String identifier;

    SuggestionType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
