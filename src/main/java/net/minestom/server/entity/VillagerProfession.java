package net.minestom.server.entity;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface VillagerProfession extends StaticProtocolObject, VillagerProfessions permits VillagerProfessionImpl {

    NetworkBuffer.Type<VillagerProfession> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(VillagerProfession::fromId, VillagerProfession::id);
    Codec<VillagerProfession> NBT_TYPE = Codec.STRING.transform(VillagerProfessionImpl::getSafe, VillagerProfession::name);

    @Contract(pure = true)
    @NotNull Registry.VillagerProfessionEntry registry();

    @Override
    default @NotNull Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }


    static @Nullable VillagerProfession fromId(int id) {
        return VillagerProfessionImpl.getId(id);
    }
}
