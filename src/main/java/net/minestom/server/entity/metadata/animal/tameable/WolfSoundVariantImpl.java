package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.sound.SoundEvent;

import java.util.Objects;

record WolfSoundVariantImpl(
        WolfSoundVariant.WolfSoundSet adultSounds,
        WolfSoundVariant.WolfSoundSet babySounds
) implements WolfSoundVariant {

    public WolfSoundVariantImpl {
        Objects.requireNonNull(adultSounds, "adultSounds");
        Objects.requireNonNull(babySounds, "babySounds");
    }

    record WolfSoundSetImpl(
            SoundEvent ambientSound,
            SoundEvent deathSound,
            SoundEvent growlSound,
            SoundEvent hurtSound,
            SoundEvent pantSound,
            SoundEvent whineSound,
            SoundEvent stepSound
    ) implements WolfSoundVariant.WolfSoundSet {
        public WolfSoundSetImpl {
            Objects.requireNonNull(ambientSound, "ambientSound");
            Objects.requireNonNull(deathSound, "deathSound");
            Objects.requireNonNull(growlSound, "growlSound");
            Objects.requireNonNull(hurtSound, "hurtSound");
            Objects.requireNonNull(pantSound, "pantSound");
            Objects.requireNonNull(whineSound, "whineSound");
            Objects.requireNonNull(stepSound, "stepSound");
        }
    }

}
