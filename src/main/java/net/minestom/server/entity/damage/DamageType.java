package net.minestom.server.entity.damage;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface DamageType extends DamageTypes permits DamageTypeImpl {
    @NotNull Codec<DamageType> REGISTRY_CODEC = StructCodec.struct(
            "message_id", Codec.STRING, DamageType::messageId,
            "scaling", Codec.STRING, DamageType::scaling,
            "exhaustion", Codec.FLOAT, DamageType::exhaustion,
            "effects", Codec.STRING.optional("hurt"), DamageType::effects,
            "death_message_type", Codec.STRING.optional("default"), DamageType::deathMessageType,
            DamageType::create);

    @NotNull Codec<RegistryKey<DamageType>> CODEC = RegistryKey.codec(Registries::damageType);

    static @NotNull DamageType create(
            @NotNull String messageId,
            @NotNull String scaling,
            float exhaustion,
            @Nullable String effects,
            @Nullable String deathMessageType
    ) {
        return new DamageTypeImpl(messageId, scaling, exhaustion, effects, deathMessageType);
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
        return DynamicRegistry.create(Key.key("minecraft:damage_type"), REGISTRY_CODEC, RegistryData.Resource.DAMAGE_TYPES);
    }

    @NotNull String messageId();

    @NotNull String scaling();

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

        public @NotNull Builder messageId(@NotNull String messageId) {
            this.messageId = messageId;
            return this;
        }

        public @NotNull Builder scaling(@NotNull String scaling) {
            this.scaling = scaling;
            return this;
        }

        public @NotNull Builder exhaustion(float exhaustion) {
            this.exhaustion = exhaustion;
            return this;
        }

        public @NotNull Builder effects(@Nullable String effects) {
            this.effects = effects;
            return this;
        }

        public @NotNull Builder deathMessageType(@Nullable String deathMessageType) {
            this.deathMessageType = deathMessageType;
            return this;
        }

        public @NotNull DamageType build() {
            return new DamageTypeImpl(messageId, scaling, exhaustion, effects, deathMessageType);
        }
    }

}