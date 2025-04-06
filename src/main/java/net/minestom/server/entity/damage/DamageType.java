package net.minestom.server.entity.damage;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface DamageType extends ProtocolObject, DamageTypes permits DamageTypeImpl {
    @NotNull Codec<DamageType> REGISTRY_CODEC = StructCodec.struct(
            "exhaustion", Codec.FLOAT, DamageType::exhaustion,
            "message_id", Codec.STRING, DamageType::messageId,
            "scaling", Codec.STRING, DamageType::scaling,
            DamageType::create);

    @NotNull Codec<DynamicRegistry.Key<DamageType>> CODEC = Codec.RegistryKey(Registries::damageType);

    static @NotNull DamageType create(
            float exhaustion,
            @NotNull String messageId,
            @NotNull String scaling
    ) {
        return new DamageTypeImpl(exhaustion, messageId, scaling);
    }

    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for damage types, loading the vanilla damage types.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<DamageType> createDefaultRegistry() {
        return DynamicRegistry.create("minecraft:damage_type", REGISTRY_CODEC, RegistryData.Resource.DAMAGE_TYPES);
    }

    float exhaustion();

    @NotNull String messageId();

    @NotNull String scaling();

    final class Builder {
        private float exhaustion = 0f;
        private String messageId;
        private String scaling;

        private Builder() {
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
            return new DamageTypeImpl(exhaustion, messageId, scaling);
        }
    }

}