package net.minestom.server.item;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

final class EnchantmentImpl implements Enchantment {
    private final Registry.EnchantmentEntry registry;

    EnchantmentImpl(Registry.EnchantmentEntry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Registry.EnchantmentEntry registry() {
        return registry;
    }
}
