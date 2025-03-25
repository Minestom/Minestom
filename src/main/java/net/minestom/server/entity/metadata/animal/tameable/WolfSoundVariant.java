package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface WolfSoundVariant extends WolfSoundVariants permits WolfSoundVariantImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<WolfSoundVariant>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::wolfSoundVariant, false);
    @NotNull Codec<DynamicRegistry.Key<WolfSoundVariant>> CODEC = Codec.RegistryKey(Registries::wolfSoundVariant);

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
        return DynamicRegistry.create("minecraft:wolf_sound_variant", REGISTRY_CODEC, Registry.Resource.WOLF_SOUND_VARIANTS);
    }

    static @NotNull WolfSoundVariant create(
            @NotNull SoundEvent ambientSound,
            @NotNull SoundEvent deathSound,
            @NotNull SoundEvent growlSound,
            @NotNull SoundEvent hurtSound,
            @NotNull SoundEvent pantSound,
            @NotNull SoundEvent whineSound
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

    static @NotNull Builder builder() {
        return new Builder();
    }

    @NotNull SoundEvent ambientSound();

    @NotNull SoundEvent deathSound();

    @NotNull SoundEvent growlSound();

    @NotNull SoundEvent hurtSound();

    @NotNull SoundEvent pantSound();

    @NotNull SoundEvent whineSound();

    final class Builder {
        private SoundEvent ambientSound;
        private SoundEvent deathSound;
        private SoundEvent growlSound;
        private SoundEvent hurtSound;
        private SoundEvent pantSound;
        private SoundEvent whineSound;

        public @NotNull Builder ambientSound(@NotNull SoundEvent ambientSound) {
            this.ambientSound = ambientSound;
            return this;
        }

        public @NotNull Builder deathSound(@NotNull SoundEvent deathSound) {
            this.deathSound = deathSound;
            return this;
        }

        public @NotNull Builder growlSound(@NotNull SoundEvent growlSound) {
            this.growlSound = growlSound;
            return this;
        }

        public @NotNull Builder hurtSound(@NotNull SoundEvent hurtSound) {
            this.hurtSound = hurtSound;
            return this;
        }

        public @NotNull Builder pantSound(@NotNull SoundEvent pantSound) {
            this.pantSound = pantSound;
            return this;
        }

        public @NotNull Builder whineSound(@NotNull SoundEvent whineSound) {
            this.whineSound = whineSound;
            return this;
        }

        public @NotNull WolfSoundVariant build() {
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
