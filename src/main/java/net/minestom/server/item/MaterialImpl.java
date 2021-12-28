package net.minestom.server.item;

import java.util.Set;

import net.minestom.server.registry.Registry;
import net.minestom.server.tags.GameTag;
import net.minestom.server.tags.GameTags;
import net.minestom.server.tags.GameTagType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record MaterialImpl(Registry.MaterialEntry registry) implements Material {
    private static final Registry.Container<Material> CONTAINER = new Registry.Container<>(Registry.Resource.ITEMS,
            (container, namespace, object) -> container.register(new MaterialImpl(Registry.material(namespace, object, null))));
    private static final Set<GameTag<Material>> TAGS = GameTags.ITEMS;

    static Material get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Material getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Material getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<Material> values() {
        return CONTAINER.values();
    }

    @Override
    public @NotNull GameTagType<Material> tagType() {
        return GameTagType.ITEMS;
    }

    @Override
    public @NotNull Set<@NotNull GameTag<Material>> tags() {
        return TAGS;
    }

    @Override
    public String toString() {
        return name();
    }
}
