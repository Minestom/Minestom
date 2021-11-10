package net.minestom.server.item;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class EnchantmentImpl implements Enchantment {
    private static final Registry.Container<Enchantment> CONTAINER = new Registry.Container<>(Registry.Resource.ENCHANTMENTS,
            (container, namespace, object) -> container.register(new EnchantmentImpl(Registry.enchantment(namespace, object, null))));

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

    private final Registry.EnchantmentEntry registry;

    EnchantmentImpl(Registry.EnchantmentEntry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Registry.EnchantmentEntry registry() {
        return registry;
    }

    @Override
    public String toString() {
        return name();
    }
}
