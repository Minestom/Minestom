package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public sealed interface Enchantment extends ProtocolObject, Enchantments permits EnchantmentImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<Enchantment>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::enchantment);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<Enchantment>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::enchantment);

    static @NotNull Builder builder(@NotNull String namespace) {
        return builder(NamespaceID.from(namespace));
    }

    static @NotNull Builder builder(@NotNull NamespaceID namespace) {
        return new Builder(namespace);
    }

    /**
     * <p>Creates a new registry for enchantments, loading the vanilla enchantments.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Enchantment> createDefaultRegistry() {
        return DynamicRegistry.create(
                "minecraft:enchantment", EnchantmentImpl.REGISTRY_NBT_TYPE
                //todo reenable to load vanilla enchants.
//                Registry.Resource.ENCHANTMENTS,
//                (namespace, props) -> new EnchantmentImpl(Registry.enchantment(namespace, props))
        );
    }

    @NotNull Component description();

    @NotNull Set<NamespaceID> exclusiveSet(); //todo read as list, single, or tag

    @NotNull Set<NamespaceID> supportedItems(); //todo read as list, single, or tag

    @NotNull Set<NamespaceID> primaryItems(); //todo read as list, single, or tag

    int weight();

    int maxLevel();

    @NotNull Cost minCost();

    @NotNull Cost maxCost();

    int anvilCost();

    @NotNull List<EquipmentSlotGroup> slots();

    @NotNull DataComponentMap effects();

    @Override
    @Nullable Registry.EnchantmentEntry registry();

    record Cost(int base, int perLevelAboveFirst) {
        public static final BinaryTagSerializer<Cost> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> new Cost(tag.getInt("base"), tag.getInt("per_level_above_first")),
                cost -> CompoundBinaryTag.builder()
                        .putInt("base", cost.base)
                        .putInt("per_level_above_first", cost.perLevelAboveFirst)
                        .build()
        );
    }

    class Builder {
        private final NamespaceID namespace;
        private Component description = Component.empty();

        private Builder(@NotNull NamespaceID namespace) {
            this.namespace = namespace;
        }

        public @NotNull Builder description(@NotNull Component description) {
            this.description = description;
            return this;
        }

        public @NotNull Enchantment build() {
            return new EnchantmentImpl(namespace, description, null);
        }
    }

}
