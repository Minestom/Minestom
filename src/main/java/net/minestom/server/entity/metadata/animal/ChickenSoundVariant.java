package net.minestom.server.entity.metadata.animal;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

public sealed interface ChickenSoundVariant extends ChickenSoundVariants permits ChickenSoundVariantImpl {
    NetworkBuffer.Type<RegistryKey<ChickenSoundVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::chickenSoundVariant);
    Codec<RegistryKey<ChickenSoundVariant>> CODEC = RegistryKey.codec(Registries::chickenSoundVariant);

    Codec<ChickenSoundVariant> REGISTRY_CODEC = StructCodec.struct(
            "adult_sounds", ChickenSoundSet.CODEC, ChickenSoundVariant::adultSounds,
            "baby_sounds", ChickenSoundSet.CODEC, ChickenSoundVariant::babySounds,
            ChickenSoundVariant::create);

    /**
     * Creates a new instance of the "minecraft:chicken_sound_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<ChickenSoundVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("chicken_sound_variant"), REGISTRY_CODEC, RegistryData.Resource.CHICKEN_SOUND_VARIANTS);
    }

    static ChickenSoundVariant create(
            ChickenSoundSet adultSounds,
            ChickenSoundSet babySounds
    ) {
        return new ChickenSoundVariantImpl(
                adultSounds,
                babySounds
        );
    }

    ChickenSoundSet adultSounds();

    ChickenSoundSet babySounds();

    sealed interface ChickenSoundSet permits ChickenSoundVariantImpl.ChickenSoundSetImpl {
        Codec<ChickenSoundSet> CODEC = StructCodec.struct(
                "ambient_sound", SoundEvent.CODEC, ChickenSoundSet::ambientSound,
                "hurt_sound", SoundEvent.CODEC, ChickenSoundSet::hurtSound,
                "death_sound", SoundEvent.CODEC, ChickenSoundSet::deathSound,
                "step_sound", SoundEvent.CODEC, ChickenSoundSet::stepSound,
                ChickenSoundSet::create);

        static ChickenSoundSet create(
                SoundEvent ambientSound,
                SoundEvent hurtSound,
                SoundEvent deathSound,
                SoundEvent stepSound
        ) {
            return new ChickenSoundVariantImpl.ChickenSoundSetImpl(
                    ambientSound,
                    hurtSound,
                    deathSound,
                    stepSound
            );
        }

        static ChickenSoundVariant.Builder builder() {
            return new ChickenSoundVariant.Builder();
        }

        SoundEvent ambientSound();

        SoundEvent hurtSound();

        SoundEvent deathSound();

        SoundEvent stepSound();

        final class Builder {
            private @UnknownNullability SoundEvent ambientSound;
            private @UnknownNullability SoundEvent deathSound;
            private @UnknownNullability SoundEvent hurtSound;
            private @UnknownNullability SoundEvent stepSound;

            public ChickenSoundSet.Builder ambientSound(SoundEvent ambientSound) {
                this.ambientSound = Objects.requireNonNull(ambientSound, "ambientSound");
                return this;
            }

            public ChickenSoundSet.Builder hurtSound(SoundEvent hurtSound) {
                this.hurtSound = Objects.requireNonNull(hurtSound, "hurtSound");
                return this;
            }

            public ChickenSoundSet.Builder deathSound(SoundEvent deathSound) {
                this.deathSound = Objects.requireNonNull(deathSound, "deathSound");
                return this;
            }

            public ChickenSoundSet.Builder stepSound(SoundEvent stepSound) {
                this.stepSound = Objects.requireNonNull(stepSound, "stepSound");
                return this;
            }

            public ChickenSoundSet build() {
                return new ChickenSoundVariantImpl.ChickenSoundSetImpl(
                        ambientSound,
                        hurtSound,
                        deathSound,
                        stepSound
                );
            }
        }
    }

    static Builder builder() {
        return new Builder();
    }

    final class Builder {
        private @UnknownNullability ChickenSoundSet adultSounds;
        private @UnknownNullability ChickenSoundSet babySounds;

        public Builder adultSounds(ChickenSoundSet adultSounds) {
            this.adultSounds = Objects.requireNonNull(adultSounds, "adultSounds");
            return this;
        }

        public Builder babySounds(ChickenSoundSet babySounds) {
            this.babySounds = Objects.requireNonNull(babySounds, "babySounds");
            return this;
        }

        public ChickenSoundVariant build() {
            return new ChickenSoundVariantImpl(
                    adultSounds,
                    babySounds
            );
        }
    }

}
