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

public sealed interface WolfSoundVariant extends WolfSoundVariants permits WolfSoundVariantImpl {
    NetworkBuffer.Type<RegistryKey<WolfSoundVariant>> NETWORK_TYPE = RegistryKey.networkType(Registries::wolfSoundVariant);
    Codec<RegistryKey<WolfSoundVariant>> CODEC = RegistryKey.codec(Registries::wolfSoundVariant);

    Codec<WolfSoundVariant> REGISTRY_CODEC = StructCodec.struct(
            "ambient_sound", SoundEvent.CODEC, WolfSoundVariant::ambientSound,
            "death_sound", SoundEvent.CODEC, WolfSoundVariant::deathSound,
            "growl_sound", SoundEvent.CODEC, WolfSoundVariant::growlSound,
            "hurt_sound", SoundEvent.CODEC, WolfSoundVariant::hurtSound,
            "pant_sound", SoundEvent.CODEC, WolfSoundVariant::pantSound,
            "whine_sound", SoundEvent.CODEC, WolfSoundVariant::whineSound,
            WolfSoundVariantImpl::new);

    /**
     * Creates a new instance of the "minecraft:wolf_sound_variant" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<WolfSoundVariant> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("minecraft:wolf_sound_variant"), REGISTRY_CODEC, RegistryData.Resource.WOLF_SOUND_VARIANTS);
    }

    static WolfSoundVariant create(
            SoundEvent ambientSound,
            SoundEvent deathSound,
            SoundEvent growlSound,
            SoundEvent hurtSound,
            SoundEvent pantSound,
            SoundEvent whineSound
    ) {
        return new WolfSoundVariantImpl(
                ambientSound,
                deathSound,
                growlSound,
                hurtSound,
                pantSound,
                whineSound
        );
    }

    static Builder builder() {
        return new Builder();
    }

    SoundEvent ambientSound();

    SoundEvent deathSound();

    SoundEvent growlSound();

    SoundEvent hurtSound();

    SoundEvent pantSound();

    SoundEvent whineSound();

    final class Builder {
        private SoundEvent ambientSound;
        private SoundEvent deathSound;
        private SoundEvent growlSound;
        private SoundEvent hurtSound;
        private SoundEvent pantSound;
        private SoundEvent whineSound;

        public Builder ambientSound(SoundEvent ambientSound) {
            this.ambientSound = ambientSound;
            return this;
        }

        public Builder deathSound(SoundEvent deathSound) {
            this.deathSound = deathSound;
            return this;
        }

        public Builder growlSound(SoundEvent growlSound) {
            this.growlSound = growlSound;
            return this;
        }

        public Builder hurtSound(SoundEvent hurtSound) {
            this.hurtSound = hurtSound;
            return this;
        }

        public Builder pantSound(SoundEvent pantSound) {
            this.pantSound = pantSound;
            return this;
        }

        public Builder whineSound(SoundEvent whineSound) {
            this.whineSound = whineSound;
            return this;
        }

        public WolfSoundVariant build() {
            return new WolfSoundVariantImpl(
                    ambientSound,
                    deathSound,
                    growlSound,
                    hurtSound,
                    pantSound,
                    whineSound
            );
        }
    }

}
