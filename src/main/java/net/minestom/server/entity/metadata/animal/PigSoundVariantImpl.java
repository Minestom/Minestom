package net.minestom.server.entity.metadata.animal;

import net.minestom.server.sound.SoundEvent;

import java.util.Objects;

record PigSoundVariantImpl(
        PigSoundVariant.PigSoundSet adultSounds,
        PigSoundVariant.PigSoundSet babySounds
) implements PigSoundVariant {

    public PigSoundVariantImpl {
        Objects.requireNonNull(adultSounds, "adultSounds");
        Objects.requireNonNull(babySounds, "babySounds");
    }

    record PigSoundSetImpl(
            SoundEvent ambientSound,
            SoundEvent hurtSound,
            SoundEvent deathSound,
            SoundEvent stepSound,
            SoundEvent eatSound
    ) implements PigSoundVariant.PigSoundSet {
        public PigSoundSetImpl {
            Objects.requireNonNull(ambientSound, "ambientSound");
            Objects.requireNonNull(hurtSound, "hurtSound");
            Objects.requireNonNull(deathSound, "deathSound");
            Objects.requireNonNull(stepSound, "stepSound");
            Objects.requireNonNull(eatSound, "eatSound");
        }
    }

}
