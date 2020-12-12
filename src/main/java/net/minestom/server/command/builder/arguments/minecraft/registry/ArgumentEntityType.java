package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.entity.EntityType;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving an {@link EntityType}.
 */
public class ArgumentEntityType extends ArgumentRegistry<EntityType> {

    public ArgumentEntityType(String id) {
        super(id);
    }

    @Override
    public EntityType getRegistry(@NotNull String value) {
        return Registries.getEntityType(value);
    }
}
