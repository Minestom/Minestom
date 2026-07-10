package net.minestom.server.entity.metadata.animal;

import net.minestom.server.sound.SoundEvent;

import java.util.Objects;

record CowSoundVariantImpl(
        CowSoundVariant.CowSoundSet adultSounds
//        CowSoundVariant.CowSoundSet babySounds
) implements CowSoundVariant {

    public CowSoundVariantImpl {
        Objects.requireNonNull(adultSounds, "adultSounds");
//        Objects.requireNonNull(babySounds, "babySounds");
    }

    record CowSoundSetImpl(
            SoundEvent ambientSound,
            SoundEvent hurtSound,
            SoundEvent deathSound,
            SoundEvent stepSound
    ) implements CowSoundVariant.CowSoundSet {
        public CowSoundSetImpl {
            Objects.requireNonNull(ambientSound, "ambientSound");
            Objects.requireNonNull(hurtSound, "hurtSound");
            Objects.requireNonNull(deathSound, "deathSound");
            Objects.requireNonNull(stepSound, "stepSound");
        }
    }

}