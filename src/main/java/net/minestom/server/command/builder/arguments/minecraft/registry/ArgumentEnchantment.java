package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.item.Enchantment;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving an {@link Enchantment}.
 */
public class ArgumentEnchantment extends ArgumentRegistry<Enchantment> {

    public ArgumentEnchantment(String id) {
        super(id);
    }

    @Override
    public String parser() {
        return "minecraft:item_enchantment";
    }

    @Override
    public Enchantment getRegistry(@NotNull String value) {
        return Enchantment.fromNamespaceId(value);
    }

    @Override
    public String toString() {
        return String.format("Enchantment<%s>", getId());
    }
}
