package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;

import java.util.Objects;

record ZombieNautilusVariantImpl(
        Model model,
        Key assetId
) implements ZombieNautilusVariant {

    public ZombieNautilusVariantImpl {
        Objects.requireNonNull(model, "Model cannot be null");
        Objects.requireNonNull(assetId, "Asset ID cannot be null");
    }
}
