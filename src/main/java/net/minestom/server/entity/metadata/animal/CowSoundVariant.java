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

/**
 * Sounds used by the cow, set with {@link net.minestom.server.component.DataComponents#COW_SOUND_VARIANT}
 * currently {@link #adultSounds()} are shared between baby and adult. This is expected to change in a future release.
 */
public sealed interface CowSoundVariant extends CowSoundVariants permits CowSoundVariantImpl {
    NetworkBuffer.Type<RegistryKey<CowSoundVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::cowSoundVariant);
    Codec<RegistryKey<CowSoundVariant>> CODEC = RegistryKey.codec(Registries::cowSoundVariant);

    Codec<CowSoundVariant> REGISTRY_CODEC = StructCodec.struct(
            StructCodec.INLINE, CowSoundSet.CODEC, CowSoundVariant::adultSounds,
            CowSoundVariant::create);

    /**
     * Creates a new instance of the "minecraft:cow_sound_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<CowSoundVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("cow_sound_variant"), REGISTRY_CODEC, RegistryData.Resource.COW_SOUND_VARIANTS);
    }

    static CowSoundVariant create(
            CowSoundSet adultSounds
    ) {
        return new CowSoundVariantImpl(
                adultSounds
        );
    }

    CowSoundSet adultSounds();

    sealed interface CowSoundSet permits CowSoundVariantImpl.CowSoundSetImpl {
        Codec<CowSoundSet> CODEC = StructCodec.struct(
                "ambient_sound", SoundEvent.CODEC, CowSoundSet::ambientSound,
                "hurt_sound", SoundEvent.CODEC, CowSoundSet::hurtSound,
                "death_sound", SoundEvent.CODEC, CowSoundSet::deathSound,
                "step_sound", SoundEvent.CODEC, CowSoundSet::stepSound,
                CowSoundSet::create);

        static CowSoundSet create(
                SoundEvent ambientSound,
                SoundEvent hurtSound,
                SoundEvent deathSound,
                SoundEvent stepSound
        ) {
            return new CowSoundVariantImpl.CowSoundSetImpl(
                    ambientSound,
                    hurtSound,
                    deathSound,
                    stepSound
            );
        }

        static CowSoundVariant.Builder builder() {
            return new CowSoundVariant.Builder();
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

            public CowSoundSet.Builder ambientSound(SoundEvent ambientSound) {
                this.ambientSound = Objects.requireNonNull(ambientSound, "ambientSound");
                return this;
            }

            public CowSoundSet.Builder hurtSound(SoundEvent hurtSound) {
                this.hurtSound = Objects.requireNonNull(hurtSound, "hurtSound");
                return this;
            }

            public CowSoundSet.Builder deathSound(SoundEvent deathSound) {
                this.deathSound = Objects.requireNonNull(deathSound, "deathSound");
                return this;
            }

            public CowSoundSet.Builder stepSound(SoundEvent stepSound) {
                this.stepSound = Objects.requireNonNull(stepSound, "stepSound");
                return this;
            }

            public CowSoundSet build() {
                return new CowSoundVariantImpl.CowSoundSetImpl(
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
        private @UnknownNullability CowSoundSet adultSounds;

        public Builder adultSounds(CowSoundSet adultSounds) {
            this.adultSounds = Objects.requireNonNull(adultSounds, "adultSounds");
            return this;
        }

        public CowSoundVariant build() {
            return new CowSoundVariantImpl(
                    adultSounds
            );
        }
    }

}
