package net.minestom.server.entity;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public sealed interface VillagerProfession extends StaticProtocolObject<VillagerProfession>, VillagerProfessions permits VillagerProfessionImpl {

    NetworkBuffer.Type<VillagerProfession> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(VillagerProfession::fromId, VillagerProfession::id);
    Codec<VillagerProfession> NBT_TYPE = Codec.STRING.transform(VillagerProfession::fromKey, VillagerProfession::name);

    @Override
    default Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    @Contract(pure = true)
    RegistryData.VillagerProfessionEntry registry();

    static Collection<VillagerProfession> values() {
        return VillagerProfessionImpl.REGISTRY.values();
    }

    static @Nullable VillagerProfession fromKey(@KeyPattern String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable VillagerProfession fromKey(Key key) {
        return VillagerProfessionImpl.REGISTRY.get(key);
    }

    static @Nullable VillagerProfession fromId(int id) {
        return VillagerProfessionImpl.REGISTRY.get(id);
    }

}
