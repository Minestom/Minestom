package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.builder.arguments.minecraft.SuggestionType;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving an {@link EntityType}.
 */
public class ArgumentEntityType extends ArgumentRegistry<EntityType> {

    public ArgumentEntityType(String id) {
        super(id);
        suggestionType = SuggestionType.SUMMONABLE_ENTITIES;
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.RESOURCE_LOCATION;
    }

    @Override
    public EntityType getRegistry(@NotNull String value) {
        return EntityType.fromKey(value);
    }

    @Override
    public String toString() {
        return String.format("EntityType<%s>", getId());
    }
}
