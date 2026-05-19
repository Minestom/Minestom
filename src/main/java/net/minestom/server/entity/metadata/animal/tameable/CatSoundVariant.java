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

public sealed interface CatSoundVariant extends CatSoundVariants permits CatSoundVariantImpl {
    NetworkBuffer.Type<RegistryKey<CatSoundVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::catSoundVariant);
    Codec<RegistryKey<CatSoundVariant>> CODEC = RegistryKey.codec(Registries::catSoundVariant);

    Codec<CatSoundVariant> REGISTRY_CODEC = StructCodec.struct(
            "adult_sounds", CatSoundSet.CODEC, CatSoundVariant::adultSounds,
            "baby_sounds", CatSoundSet.CODEC, CatSoundVariant::babySounds,
            CatSoundVariant::create);

    /**
     * Creates a new instance of the "minecraft:cat_sound_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<CatSoundVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("cat_sound_variant"), REGISTRY_CODEC, RegistryData.Resource.CAT_SOUND_VARIANTS);
    }

    static CatSoundVariant create(
            CatSoundSet adultSounds,
            CatSoundSet babySounds
    ) {
        return new CatSoundVariantImpl(
                adultSounds,
                babySounds
        );
    }

    static Builder builder() {
        return new Builder();
    }

    CatSoundSet adultSounds();

    CatSoundSet babySounds();

    sealed interface CatSoundSet permits CatSoundVariantImpl.CatSoundSetImpl {
        Codec<CatSoundSet> CODEC = StructCodec.struct(
                "ambient_sound", SoundEvent.CODEC, CatSoundSet::ambientSound,
                "stray_ambient_sound", SoundEvent.CODEC, CatSoundSet::deathSound,
                "hiss_sound", SoundEvent.CODEC, CatSoundSet::hissSound,
                "hurt_sound", SoundEvent.CODEC, CatSoundSet::hurtSound,
                "death_sound", SoundEvent.CODEC, CatSoundSet::deathSound,
                "eat_sound", SoundEvent.CODEC, CatSoundSet::eatSound,
                "beg_for_food_sound", SoundEvent.CODEC, CatSoundSet::begForFoodSound,
                "purr_sound", SoundEvent.CODEC, CatSoundSet::purrSound,
                "purreow_sound", SoundEvent.CODEC, CatSoundSet::purreowSound,
                CatSoundSet::create);

        static CatSoundSet create(
                SoundEvent ambientSound,
                SoundEvent strayAmbientSound,
                SoundEvent hissSound,
                SoundEvent hurtSound,
                SoundEvent deathSound,
                SoundEvent eatSound,
                SoundEvent begForFoodSound,
                SoundEvent purrSound,
                SoundEvent purreowSound
        ) {
            return new CatSoundVariantImpl.CatSoundSetImpl(
                    ambientSound,
                    strayAmbientSound,
                    hissSound,
                    hurtSound,
                    deathSound,
                    eatSound,
                    begForFoodSound,
                    purrSound,
                    purreowSound
            );
        }

        static CatSoundVariant.Builder builder() {
            return new CatSoundVariant.Builder();
        }

        SoundEvent ambientSound();

        SoundEvent strayAmbientSound();

        SoundEvent hissSound();

        SoundEvent hurtSound();

        SoundEvent deathSound();

        SoundEvent eatSound();

        SoundEvent begForFoodSound();

        SoundEvent purrSound();

        SoundEvent purreowSound();

        final class Builder {
            private @UnknownNullability SoundEvent ambientSound;
            private @UnknownNullability SoundEvent strayAmbientSound;
            private @UnknownNullability SoundEvent hissSound;
            private @UnknownNullability SoundEvent hurtSound;
            private @UnknownNullability SoundEvent deathSound;
            private @UnknownNullability SoundEvent eatSound;
            private @UnknownNullability SoundEvent begForFoodSound;
            private @UnknownNullability SoundEvent purrSound;
            private @UnknownNullability SoundEvent purreowSound;

            public CatSoundSet.Builder ambientSound(SoundEvent ambientSound) {
                this.ambientSound = Objects.requireNonNull(ambientSound, "ambientSound");
                return this;
            }

            public CatSoundSet.Builder strayAmbientSound(SoundEvent strayAmbientSound) {
                this.strayAmbientSound = Objects.requireNonNull(strayAmbientSound, "strayAmbientSound");
                return this;
            }

            public CatSoundSet.Builder hissSound(SoundEvent hissSound) {
                this.hissSound = Objects.requireNonNull(hissSound, "hissSound");
                return this;
            }

            public CatSoundSet.Builder hurtSound(SoundEvent hurtSound) {
                this.hurtSound = Objects.requireNonNull(hurtSound, "hurtSound");
                return this;
            }

            public CatSoundSet.Builder deathSound(SoundEvent deathSound) {
                this.deathSound = Objects.requireNonNull(deathSound, "deathSound");
                return this;
            }

            public CatSoundSet.Builder eatSound(SoundEvent eatSound) {
                this.eatSound = Objects.requireNonNull(eatSound, "eatSound");
                return this;
            }

            public CatSoundSet.Builder begForFoodSound(SoundEvent begForFoodSound) {
                this.begForFoodSound = Objects.requireNonNull(begForFoodSound, "begForFoodSound");
                return this;
            }

            public CatSoundSet.Builder purrSound(SoundEvent purrSound) {
                this.purrSound = Objects.requireNonNull(purrSound, "purrSound");
                return this;
            }

            public CatSoundSet.Builder purreowSound(SoundEvent purreowSound) {
                this.purreowSound = Objects.requireNonNull(purreowSound, "purreowSound");
                return this;
            }

            public CatSoundSet build() {
                return new CatSoundVariantImpl.CatSoundSetImpl(
                        ambientSound,
                        strayAmbientSound,
                        hissSound,
                        hurtSound,
                        deathSound,
                        eatSound,
                        begForFoodSound,
                        purrSound,
                        purreowSound
                );
            }
        }
    }

    final class Builder {
        private @UnknownNullability CatSoundSet adultSounds;
        private @UnknownNullability CatSoundSet babySounds;

        public Builder adultSounds(CatSoundSet adultSounds) {
            this.adultSounds = Objects.requireNonNull(adultSounds, "adultSounds");
            return this;
        }

        public Builder babySounds(CatSoundSet babySounds) {
            this.babySounds = Objects.requireNonNull(babySounds, "babySounds");
            return this;
        }

        public CatSoundVariant build() {
            return new CatSoundVariantImpl(
                    adultSounds,
                    babySounds
            );
        }
    }

}
