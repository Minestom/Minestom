package net.minestom.server.item.enchant;

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

    @Override
    @Nullable Registry.EnchantmentEntry registry();

    class Builder {
        private final NamespaceID namespace;

        private Builder(@NotNull NamespaceID namespace) {
            this.namespace = namespace;
        }

        public @NotNull Enchantment build() {
            return new EnchantmentImpl(namespace, null);
        }
    }

}
