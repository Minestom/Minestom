package net.minestom.server.entity.damage;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public sealed interface DamageType extends DamageTypes permits DamageTypeImpl {
    Codec<DamageType> REGISTRY_CODEC = StructCodec.struct(
            "message_id", Codec.STRING, DamageType::messageId,
            "scaling", Codec.STRING, DamageType::scaling,
            "exhaustion", Codec.FLOAT, DamageType::exhaustion,
            "effects", Codec.STRING.optional("hurt"), DamageType::effects,
            "death_message_type", Codec.STRING.optional("default"), DamageType::deathMessageType,
            DamageType::create);

    Codec<RegistryKey<DamageType>> CODEC = RegistryKey.codec(Registries::damageType);

    static DamageType create(
            String messageId,
            String scaling,
            float exhaustion,
            @Nullable String effects,
            @Nullable String deathMessageType
    ) {
        return new DamageTypeImpl(messageId, scaling, exhaustion, effects, deathMessageType);
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for damage types, loading the vanilla damage types.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<DamageType> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("minecraft:damage_type"), REGISTRY_CODEC, RegistryData.Resource.DAMAGE_TYPES);
    }

    String messageId();

    String scaling();

    float exhaustion();

    @Nullable String effects();

    @Nullable String deathMessageType();

    final class Builder {
        private String messageId;
        private String scaling;
        private float exhaustion = 0f;
        private String effects;
        private String deathMessageType;

        private Builder() {
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder scaling(String scaling) {
            this.scaling = scaling;
            return this;
        }

        public Builder exhaustion(float exhaustion) {
            this.exhaustion = exhaustion;
            return this;
        }

        public Builder effects(@Nullable String effects) {
            this.effects = effects;
            return this;
        }

        public Builder deathMessageType(@Nullable String deathMessageType) {
            this.deathMessageType = deathMessageType;
            return this;
        }

        public DamageType build() {
            return new DamageTypeImpl(messageId, scaling, exhaustion, effects, deathMessageType);
        }
    }

}