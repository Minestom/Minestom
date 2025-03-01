package net.minestom.server.item;

import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;

public sealed interface Material extends StaticProtocolObject, Materials permits MaterialImpl {

    NetworkBuffer.Type<Material> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(MaterialImpl::getId, Material::id);
    BinaryTagSerializer<Material> NBT_TYPE = BinaryTagSerializer.STRING.map(MaterialImpl::getSafe, Material::name);

    /**
     * Returns the raw registry data for the material.
     */
    @Contract(pure = true)
    @NotNull Registry.MaterialEntry registry();

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
        return prototype().get(ItemComponent.MAX_STACK_SIZE, 64);
    }

    static @NotNull Collection<@NotNull Material> values() {
        return MaterialImpl.values();
    }

    static @Nullable Material fromKey(@NotNull String key) {
        return MaterialImpl.getSafe(key);
    }

    static @Nullable Material fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    static @Nullable Material fromId(int id) {
        return MaterialImpl.getId(id);
    }
}
