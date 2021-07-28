package net.minestom.server.potion;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

final class PotionTypeImpl implements PotionType {
    private final NamespaceID namespaceID;
    private final int id;

    PotionTypeImpl(NamespaceID namespaceID, int id) {
        this.namespaceID = namespaceID;
        this.id = id;
    }

    @Override
    public @NotNull NamespaceID namespace() {
        return namespaceID;
    }

    @Override
    public int id() {
        return id;
    }
}
