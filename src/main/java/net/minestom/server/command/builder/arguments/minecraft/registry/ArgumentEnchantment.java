package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.item.Enchantment;
import net.minestom.server.registry.Registries;

/**
 * Represents an argument giving an {@link Enchantment}.
 */
public class ArgumentEnchantment extends ArgumentRegistry<Enchantment> {

    public ArgumentEnchantment(String id) {
        super(id);
    }

    @Override
    public Enchantment getRegistry(String value) {
        return Registries.getEnchantment(value);
    }
}
