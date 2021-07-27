package net.minestom.server.item;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

final class MaterialImpl implements Material {
    private final Registry.MaterialEntry registry;

    MaterialImpl(Registry.MaterialEntry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Registry.MaterialEntry registry() {
        return registry;
    }
}
