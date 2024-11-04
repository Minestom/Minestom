package net.minestom.server.entity;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface VillagerProfession extends StaticProtocolObject, VillagerProfessions permits VillagerProfessionImpl {

    NetworkBuffer.Type<VillagerProfession> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(VillagerProfession::fromId, VillagerProfession::id);
    BinaryTagSerializer<VillagerProfession> NBT_TYPE = BinaryTagSerializer.STRING.map(VillagerProfessionImpl::getSafe, VillagerProfession::name);

    @Contract(pure = true)
    @NotNull Registry.VillagerProfessionEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    @Override
    default int id() {
        return registry().id();
    }


    static @Nullable VillagerProfession fromId(int id) {
        return VillagerProfessionImpl.getId(id);
    }
}
