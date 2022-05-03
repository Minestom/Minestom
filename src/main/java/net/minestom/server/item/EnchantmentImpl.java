package net.minestom.server.item;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record EnchantmentImpl(Registry.EnchantmentEntry registry) implements Enchantment {
    private static final Registry.Container<Enchantment> CONTAINER = Registry.createContainer(Registry.Resource.ENCHANTMENTS,
            (namespace, properties) -> new EnchantmentImpl(Registry.enchantment(namespace, properties)));

    static Enchantment get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Enchantment getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Enchantment getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<Enchantment> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
