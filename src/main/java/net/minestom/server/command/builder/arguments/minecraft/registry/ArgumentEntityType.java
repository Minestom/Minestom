package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.entity.EntityType;
import net.minestom.server.registry.Registries;

/**
 * Represent an argument giving an entity type
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
