package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument giving an {@link Enchantment}.
 */
public class ArgumentEnchantment extends ArgumentRegistry<DynamicRegistry.Key<Enchantment>> {

    public ArgumentEnchantment(String id) {
        super(id);
    }

    @Override
    public String parser() {
        return "minecraft:item_enchantment";
    }

    @Override
    public DynamicRegistry.Key<Enchantment> getRegistry(@NotNull String value) {
//        return Enchantment.fromNamespaceId(value);
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public String toString() {
        return String.format("Enchantment<%s>", getId());
    }
}
