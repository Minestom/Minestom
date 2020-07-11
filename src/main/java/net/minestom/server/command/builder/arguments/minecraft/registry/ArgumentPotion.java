package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.Registries;

/**
 * Represent an argument giving a potion type
 */
public class ArgumentPotion extends ArgumentRegistry<PotionType> {

    public ArgumentPotion(String id) {
        super(id);
    }

    @Override
    public PotionType getRegistry(String value) {
        return Registries.getPotionType(value);
    }
}
