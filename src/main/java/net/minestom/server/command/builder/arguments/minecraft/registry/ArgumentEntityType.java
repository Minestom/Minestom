package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.entity.EntityType;
import net.minestom.server.registry.Registries;

/**
 * Represents an argument giving an {@link EntityType}.
 */
public class ArgumentEntityType extends ArgumentRegistry<EntityType> {

    public ArgumentEntityType(String id) {
        super(id);
    }

    @Override
    public EntityType getRegistry(String value) {
        return Registries.getEntityType(value);
    }
}
