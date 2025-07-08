package net.minestom.server.entity;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface VillagerProfession extends StaticProtocolObject<VillagerProfession>, VillagerProfessions permits VillagerProfessionImpl {

    NetworkBuffer.Type<VillagerProfession> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(VillagerProfession::fromId, VillagerProfession::id);
    Codec<VillagerProfession> NBT_TYPE = Codec.STRING.transform(VillagerProfession::fromKey, VillagerProfession::name);

    @Override
    default @NotNull Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    @Contract(pure = true)
    @NotNull RegistryData.VillagerProfessionEntry registry();

    static @NotNull Collection<@NotNull VillagerProfession> values() {
        return VillagerProfessionImpl.REGISTRY.values();
    }

    static @Nullable VillagerProfession fromKey(@KeyPattern @NotNull String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable VillagerProfession fromKey(@NotNull Key key) {
        return VillagerProfessionImpl.REGISTRY.get(key);
    }

    static @Nullable VillagerProfession fromId(int id) {
        return VillagerProfessionImpl.REGISTRY.get(id);
    }

    static @NotNull Registry<VillagerProfession> staticRegistry() {
        return VillagerProfessionImpl.REGISTRY;
    }
}
