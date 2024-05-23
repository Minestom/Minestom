package net.minestom.server.entity.damage;

import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface DamageType extends ProtocolObject, DamageTypes permits DamageTypeImpl {

    static @NotNull DamageType create(
            @NotNull NamespaceID namespace,
            float exhaustion,
            @NotNull String messageId,
            @NotNull String scaling
    ) {
        return new DamageTypeImpl(namespace, exhaustion, messageId, scaling, null);
    }

    static @NotNull Builder builder(@NotNull String namespace) {
        return builder(NamespaceID.from(namespace));
    }

    static @NotNull Builder builder(@NotNull NamespaceID namespace) {
        return new Builder(namespace);
    }

    /**
     * <p>Creates a new registry for damage types, loading the vanilla damage types.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<DamageType> createDefaultRegistry() {
        return DynamicRegistry.create(
                "minecraft:damage_type", DamageTypeImpl.REGISTRY_NBT_TYPE, Registry.Resource.DAMAGE_TYPES,
                (namespace, props) -> new DamageTypeImpl(Registry.damageType(namespace, props))
        );
    }

    float exhaustion();

    @NotNull String messageId();

    @NotNull String scaling();

    @Nullable Registry.DamageTypeEntry registry();

    final class Builder {
        private final NamespaceID namespace;
        private float exhaustion = 0f;
        private String messageId;
        private String scaling;

        public Builder(@NotNull NamespaceID namespace) {
            this.namespace = namespace;
        }

        public @NotNull Builder exhaustion(float exhaustion) {
            this.exhaustion = exhaustion;
            return this;
        }

        public @NotNull Builder messageId(@NotNull String messageId) {
            this.messageId = messageId;
            return this;
        }

        public @NotNull Builder scaling(@NotNull String scaling) {
            this.scaling = scaling;
            return this;
        }

        public @NotNull DamageType build() {
            return new DamageTypeImpl(namespace, exhaustion, messageId, scaling, null);
        }
    }

}