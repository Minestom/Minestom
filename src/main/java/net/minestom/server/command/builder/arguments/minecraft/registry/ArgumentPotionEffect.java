package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.Registries;

/**
 * Represent an argument giving a potion type
 */
public class ArgumentPotionEffect extends ArgumentRegistry<PotionEffect> {

    public ArgumentPotionEffect(String id) {
        super(id);
    }

    @Override
    public PotionEffect getRegistry(String value) {
        return Registries.getPotionEffect(value);
    }
}
