package net.minestom.server.entity.metadata.cube;

import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

record SulfurCubeArchetypeImpl(
        RegistryTag<Material> items,
        List<AttributeEntry> attributeModifiers,
        boolean buoyant,
        @Nullable Explosion explosion,
        @Nullable ContactDamage contactDamage,
        KnockbackModifiers knockbackModifiers,
        SoundSettings soundSettings
) implements SulfurCubeArchetype {

    public SulfurCubeArchetypeImpl {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(attributeModifiers, "attributeModifiers");
        Objects.requireNonNull(knockbackModifiers, "knockbackModifiers");
        Objects.requireNonNull(soundSettings, "soundSettings");
    }

}