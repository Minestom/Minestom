package net.minestom.server.entity.villager;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface VillagerType extends StaticProtocolObject, VillagerTypes permits VillagerTypeImpl {

    static @NotNull Collection<@NotNull VillagerType> values() {
        return VillagerTypeImpl.values();
    }

    static @Nullable VillagerType fromNamespaceId(@NotNull String namespaceID) {
        return VillagerTypeImpl.getSafe(namespaceID);
    }

    static @Nullable VillagerType fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable VillagerType fromId(int id) {
        return VillagerTypeImpl.getId(id);
    }

    @Override
    default @NotNull Key key() {
        return StaticProtocolObject.super.key();
    }

    @Contract(pure = true)
    Registry.VillagerType registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }
}
