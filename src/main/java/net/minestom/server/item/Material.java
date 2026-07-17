package net.minestom.server.item;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.translation.Translatable;
import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Material extends StaticProtocolObject<Material>, Materials, Translatable permits MaterialImpl {

    NetworkBuffer.Type<Material> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(Material::fromId, Material::id);
    Codec<Material> CODEC = Codec.KEY.transform(Material::fromKey, Material::key);

    /**
     * Returns the raw registry data for the material.
     *
     * @return the legacy registry data
     * @deprecated use the direct accessors on {@link Material}
     */
    @Deprecated(forRemoval = true)
    @Contract(pure = true)
    RegistryData.MaterialEntry registry();

    @Override
    default Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    /**
     * @deprecated use {@code block() != null}
     */
    @Deprecated(forRemoval = true)
    @Contract(pure = true)
    default boolean isBlock() {
        return block() != null;
    }

    /**
     * Returns the block corresponding to this material.
     *
     * @return the corresponding block, or {@code null} when this material is not a block item
     */
    @Contract(pure = true)
    default @Nullable Block block() {
        return registry().block();
    }

    /**
     * Returns the process-global vanilla component prototype for this material.
     *
     * @return the material component prototype
     * @throws IllegalStateException if vanilla registries have not bound material prototypes yet
     */
    @Contract(pure = true)
    default DataComponentMap prototype() {
        return registry().prototype();
    }

    /**
     * Returns whether this material equips into an armor slot.
     *
     * @return {@code true} if this material is armor
     */
    @Contract(pure = true)
    default boolean armor() {
        return registry().isArmor();
    }

    /**
     * @deprecated use {@link #armor()}
     */
    @Deprecated(forRemoval = true)
    @Contract(pure = true)
    default boolean isArmor() {
        return armor();
    }

    /**
     * Returns the equipment slot used by this material.
     *
     * @return the equipment slot, or {@code null} when this material is not equippable
     */
    @Contract(pure = true)
    default @Nullable EquipmentSlot equipmentSlot() {
        return registry().equipmentSlot();
    }

    @Override
    default String translationKey() {
        return registry().translationKey();
    }

    @Contract(pure = true)
    default int maxStackSize() {
        return prototype().get(DataComponents.MAX_STACK_SIZE, 64);
    }

    static Collection<Material> values() {
        return MaterialImpl.REGISTRY.values();
    }

    static @Nullable Material fromKey(@KeyPattern String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable Material fromKey(Key key) {
        return MaterialImpl.REGISTRY.get(key);
    }

    static @Nullable Material fromId(int id) {
        return MaterialImpl.REGISTRY.get(id);
    }

    static Registry<Material> staticRegistry() {
        return MaterialImpl.REGISTRY;
    }
}
