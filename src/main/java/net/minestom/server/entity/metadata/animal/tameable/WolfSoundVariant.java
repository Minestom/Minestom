package net.minestom.server.entity.metadata.animal.tameable;

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

public sealed interface WolfSoundVariant extends WolfSoundVariants permits WolfSoundVariantImpl {
    NetworkBuffer.Type<RegistryKey<WolfSoundVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::wolfSoundVariant);
    Codec<RegistryKey<WolfSoundVariant>> CODEC = RegistryKey.codec(Registries::wolfSoundVariant);

    Codec<WolfSoundVariant> REGISTRY_CODEC = StructCodec.struct(
            "adult_sounds", WolfSoundSet.CODEC, WolfSoundVariant::adultSounds,
            "baby_sounds", WolfSoundSet.CODEC, WolfSoundVariant::babySounds,
            WolfSoundVariant::create);

    /**
     * Creates a new instance of the "minecraft:wolf_sound_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<WolfSoundVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("wolf_sound_variant"), REGISTRY_CODEC, RegistryData.Resource.WOLF_SOUND_VARIANTS);
    }

    static WolfSoundVariant create(
            WolfSoundSet adultSounds,
            WolfSoundSet babySounds
    ) {
        return new WolfSoundVariantImpl(
                adultSounds,
                babySounds
        );
    }

    WolfSoundSet adultSounds();

    WolfSoundSet babySounds();

    sealed interface WolfSoundSet permits WolfSoundVariantImpl.WolfSoundSetImpl {
        Codec<WolfSoundSet> CODEC = StructCodec.struct(
                "ambient_sound", SoundEvent.CODEC, WolfSoundSet::ambientSound,
                "death_sound", SoundEvent.CODEC, WolfSoundSet::deathSound,
                "growl_sound", SoundEvent.CODEC, WolfSoundSet::growlSound,
                "hurt_sound", SoundEvent.CODEC, WolfSoundSet::hurtSound,
                "pant_sound", SoundEvent.CODEC, WolfSoundSet::pantSound,
                "whine_sound", SoundEvent.CODEC, WolfSoundSet::whineSound,
                "step_sound", SoundEvent.CODEC, WolfSoundSet::stepSound,
                WolfSoundSet::create);

        static WolfSoundSet create(
                SoundEvent ambientSound,
                SoundEvent deathSound,
                SoundEvent growlSound,
                SoundEvent hurtSound,
                SoundEvent pantSound,
                SoundEvent whineSound,
                SoundEvent stepSound
        ) {
            return new WolfSoundVariantImpl.WolfSoundSetImpl(
                    ambientSound,
                    deathSound,
                    growlSound,
                    hurtSound,
                    pantSound,
                    whineSound,
                    stepSound
            );
        }

        static WolfSoundVariant.Builder builder() {
            return new WolfSoundVariant.Builder();
        }

        SoundEvent ambientSound();

        SoundEvent deathSound();

        SoundEvent growlSound();

        SoundEvent hurtSound();

        SoundEvent pantSound();

        SoundEvent whineSound();

        SoundEvent stepSound();

        final class Builder {
            private @UnknownNullability SoundEvent ambientSound;
            private @UnknownNullability SoundEvent deathSound;
            private @UnknownNullability SoundEvent growlSound;
            private @UnknownNullability SoundEvent hurtSound;
            private @UnknownNullability SoundEvent pantSound;
            private @UnknownNullability SoundEvent whineSound;
            private @UnknownNullability SoundEvent stepSound;

            public WolfSoundSet.Builder ambientSound(SoundEvent ambientSound) {
                this.ambientSound = Objects.requireNonNull(ambientSound, "ambientSound");
                return this;
            }

            public WolfSoundSet.Builder deathSound(SoundEvent deathSound) {
                this.deathSound = Objects.requireNonNull(deathSound, "deathSound");
                return this;
            }

            public WolfSoundSet.Builder growlSound(SoundEvent growlSound) {
                this.growlSound = Objects.requireNonNull(growlSound, "growlSound");
                return this;
            }

            public WolfSoundSet.Builder hurtSound(SoundEvent hurtSound) {
                this.hurtSound = Objects.requireNonNull(hurtSound, "hurtSound");
                return this;
            }

            public WolfSoundSet.Builder pantSound(SoundEvent pantSound) {
                this.pantSound = Objects.requireNonNull(pantSound, "pantSound");
                return this;
            }

            public WolfSoundSet.Builder whineSound(SoundEvent whineSound) {
                this.whineSound = Objects.requireNonNull(whineSound, "whineSound");
                return this;
            }

            public WolfSoundSet.Builder stepSound(SoundEvent stepSound) {
                this.stepSound = Objects.requireNonNull(stepSound, "stepSound");
                return this;
            }

            public WolfSoundSet build() {
                return new WolfSoundVariantImpl.WolfSoundSetImpl(
                        ambientSound,
                        deathSound,
                        growlSound,
                        hurtSound,
                        pantSound,
                        whineSound,
                        stepSound
                );
            }
        }
    }

    static Builder builder() {
        return new Builder();
    }

    final class Builder {
        private @UnknownNullability WolfSoundSet adultSounds;
        private @UnknownNullability WolfSoundSet babySounds;

        public Builder adultSounds(WolfSoundSet adultSounds) {
            this.adultSounds = Objects.requireNonNull(adultSounds, "adultSounds");
            return this;
        }

        public Builder babySounds(WolfSoundSet babySounds) {
            this.babySounds = Objects.requireNonNull(babySounds, "babySounds");
            return this;
        }

        public WolfSoundVariant build() {
            return new WolfSoundVariantImpl(
                    adultSounds,
                    babySounds
            );
        }
    }

}
