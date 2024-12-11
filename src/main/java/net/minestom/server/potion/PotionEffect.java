package net.minestom.server.potion;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface PotionEffect extends StaticProtocolObject, PotionEffects permits PotionEffectImpl {

    NetworkBuffer.Type<PotionEffect> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(PotionEffectImpl::getId, PotionEffect::id);

    @Contract(pure = true)
    @NotNull Registry.PotionEffectEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    @Override
    default int id() {
        return registry().id();
    }

    static @NotNull Collection<@NotNull PotionEffect> values() {
        return PotionEffectImpl.values();
    }

    static @Nullable PotionEffect fromNamespaceId(@NotNull String namespaceID) {
        return PotionEffectImpl.getSafe(namespaceID);
    }

    static @Nullable PotionEffect fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable PotionEffect fromId(int id) {
        return PotionEffectImpl.getId(id);
    }
}
