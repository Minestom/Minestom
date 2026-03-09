package net.minestom.server.entity.metadata.animal;

import net.minestom.server.sound.SoundEvent;

import java.util.Objects;

record ChickenSoundVariantImpl(
        ChickenSoundVariant.ChickenSoundSet adultSounds,
        ChickenSoundVariant.ChickenSoundSet babySounds
) implements ChickenSoundVariant {

    public ChickenSoundVariantImpl {
        Objects.requireNonNull(adultSounds, "adultSounds");
        Objects.requireNonNull(babySounds, "babySounds");
    }

    record ChickenSoundSetImpl(
            SoundEvent ambientSound,
            SoundEvent hurtSound,
            SoundEvent deathSound,
            SoundEvent stepSound
    ) implements ChickenSoundVariant.ChickenSoundSet {
        public ChickenSoundSetImpl {
            Objects.requireNonNull(ambientSound, "ambientSound");
            Objects.requireNonNull(hurtSound, "hurtSound");
            Objects.requireNonNull(deathSound, "deathSound");
            Objects.requireNonNull(stepSound, "stepSound");
        }
    }

}
