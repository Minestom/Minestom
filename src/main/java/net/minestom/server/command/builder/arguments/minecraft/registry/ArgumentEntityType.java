package net.minestom.server.command.builder.arguments.minecraft.registry;

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
    public String parser() {
        return "minecraft:resource_location";
    }

    @Override
    public EntityType getRegistry(@NotNull String value) {
        return EntityType.fromNamespaceId(value);
    }

    @Override
    public String toString() {
        return String.format("EntityType<%s>", getId());
    }
}
