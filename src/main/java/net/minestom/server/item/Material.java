package net.minestom.server.item;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;

public sealed interface Material extends StaticProtocolObject, Materials permits MaterialImpl {

    NetworkBuffer.Type<Material> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(Material::fromId, Material::id);
    Codec<Material> CODEC = Codec.KEY.transform(Material::fromKey, Material::key);

    /**
     * Returns the raw registry data for the material.
     */
    @Contract(pure = true)
    @NotNull RegistryData.MaterialEntry registry();

    @Override
    default @NotNull Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    default boolean isBlock() {
        return registry().block() != null;
    }

    default @UnknownNullability Block block() {
        return registry().block();
    }

    default @NotNull DataComponentMap prototype() {
        return registry().prototype();
    }

    default boolean isArmor() {
        return registry().isArmor();
    }

    default int maxStackSize() {
        return prototype().get(DataComponents.MAX_STACK_SIZE, 64);
    }

    static @NotNull Collection<@NotNull Material> values() {
        return MaterialImpl.REGISTRY.values();
    }

    static @Nullable Material fromKey(@KeyPattern @NotNull String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable Material fromKey(@NotNull Key key) {
        return MaterialImpl.REGISTRY.get(key);
    }

    static @Nullable Material fromId(int id) {
        return MaterialImpl.REGISTRY.get(id);
    }
}
