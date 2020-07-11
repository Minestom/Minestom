package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.item.Enchantment;
import net.minestom.server.registry.Registries;

/**
 * Represent an argument giving an item enchantment
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
