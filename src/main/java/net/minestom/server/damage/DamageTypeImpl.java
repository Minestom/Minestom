package net.minestom.server.damage;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record DamageTypeImpl(Registry.DamageTypeEntry registry) implements DamageType {
    private static final Registry.Container<DamageType> CONTAINER = Registry.createContainer(Registry.Resource.DAMAGE_TYPES,
            (namespace, properties) -> new DamageTypeImpl(Registry.damageType(namespace, properties)));

    static DamageType get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static DamageType getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static DamageType getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<DamageType> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public int id() {
        return CONTAINER.toId(name());
    }
}
