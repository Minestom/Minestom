package net.minestom.server.entity.villager;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface VillagerProfession extends StaticProtocolObject, VillagerProfessions {
    static @NotNull Collection<@NotNull VillagerProfession> values() {
        return VillagerProfessionImpl.values();
    }

    static @Nullable VillagerProfession fromNamespaceId(@NotNull String namespaceID) {
        return VillagerProfessionImpl.getSafe(namespaceID);
    }

    static @Nullable VillagerProfession fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable VillagerProfession fromId(int id) {
        return VillagerProfessionImpl.getId(id);
    }

    @Override
    default @NotNull Key key() {
        return StaticProtocolObject.super.key();
    }

    @Contract(pure = true)
    Registry.VillagerProfession registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }
}
