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

public sealed interface PigSoundVariant extends PigSoundVariants permits PigSoundVariantImpl {
    NetworkBuffer.Type<RegistryKey<PigSoundVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::pigSoundVariant);
    Codec<RegistryKey<PigSoundVariant>> CODEC = RegistryKey.codec(Registries::pigSoundVariant);

    Codec<PigSoundVariant> REGISTRY_CODEC = StructCodec.struct(
            "adult_sounds", PigSoundSet.CODEC, PigSoundVariant::adultSounds,
            "baby_sounds", PigSoundSet.CODEC, PigSoundVariant::babySounds,
            PigSoundVariant::create);

    /**
     * Creates a new instance of the "minecraft:pig_sound_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<PigSoundVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("pig_sound_variant"), REGISTRY_CODEC, RegistryData.Resource.PIG_SOUND_VARIANTS);
    }

    static PigSoundVariant create(
            PigSoundSet adultSounds,
            PigSoundSet babySounds
    ) {
        return new PigSoundVariantImpl(
                adultSounds,
                babySounds
        );
    }

    PigSoundSet adultSounds();

    PigSoundSet babySounds();

    sealed interface PigSoundSet permits PigSoundVariantImpl.PigSoundSetImpl {
        Codec<PigSoundSet> CODEC = StructCodec.struct(
                "ambient_sound", SoundEvent.CODEC, PigSoundSet::ambientSound,
                "hurt_sound", SoundEvent.CODEC, PigSoundSet::hurtSound,
                "death_sound", SoundEvent.CODEC, PigSoundSet::deathSound,
                "step_sound", SoundEvent.CODEC, PigSoundSet::stepSound,
                "eat_sound", SoundEvent.CODEC, PigSoundSet::eatSound,
                PigSoundSet::create);

        static PigSoundSet create(
                SoundEvent ambientSound,
                SoundEvent hurtSound,
                SoundEvent deathSound,
                SoundEvent stepSound,
                SoundEvent eatSound
        ) {
            return new PigSoundVariantImpl.PigSoundSetImpl(
                    ambientSound,
                    hurtSound,
                    deathSound,
                    stepSound,
                    eatSound
            );
        }

        static PigSoundVariant.Builder builder() {
            return new PigSoundVariant.Builder();
        }

        SoundEvent ambientSound();

        SoundEvent hurtSound();

        SoundEvent deathSound();

        SoundEvent stepSound();

        SoundEvent eatSound();

        final class Builder {
            private @UnknownNullability SoundEvent ambientSound;
            private @UnknownNullability SoundEvent deathSound;
            private @UnknownNullability SoundEvent hurtSound;
            private @UnknownNullability SoundEvent stepSound;
            private @UnknownNullability SoundEvent eatSound;

            public PigSoundSet.Builder ambientSound(SoundEvent ambientSound) {
                this.ambientSound = Objects.requireNonNull(ambientSound, "ambientSound");
                return this;
            }

            public PigSoundSet.Builder hurtSound(SoundEvent hurtSound) {
                this.hurtSound = Objects.requireNonNull(hurtSound, "hurtSound");
                return this;
            }

            public PigSoundSet.Builder deathSound(SoundEvent deathSound) {
                this.deathSound = Objects.requireNonNull(deathSound, "deathSound");
                return this;
            }

            public PigSoundSet.Builder stepSound(SoundEvent stepSound) {
                this.stepSound = Objects.requireNonNull(stepSound, "stepSound");
                return this;
            }

            public PigSoundSet.Builder eatSound(SoundEvent eatSound) {
                this.eatSound = Objects.requireNonNull(eatSound, "eatSound");
                return this;
            }

            public PigSoundSet build() {
                return new PigSoundVariantImpl.PigSoundSetImpl(
                        ambientSound,
                        hurtSound,
                        deathSound,
                        stepSound,
                        eatSound
                );
            }
        }
    }

    static Builder builder() {
        return new Builder();
    }

    final class Builder {
        private @UnknownNullability PigSoundSet adultSounds;
        private @UnknownNullability PigSoundSet babySounds;

        public Builder adultSounds(PigSoundSet adultSounds) {
            this.adultSounds = Objects.requireNonNull(adultSounds, "adultSounds");
            return this;
        }

        public Builder babySounds(PigSoundSet babySounds) {
            this.babySounds = Objects.requireNonNull(babySounds, "babySounds");
            return this;
        }

        public PigSoundVariant build() {
            return new PigSoundVariantImpl(
                    adultSounds,
                    babySounds
            );
        }
    }

}
