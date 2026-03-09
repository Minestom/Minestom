package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.sound.SoundEvent;

import java.util.Objects;

record CatSoundVariantImpl(
        CatSoundVariant.CatSoundSet adultSounds,
        CatSoundVariant.CatSoundSet babySounds
) implements CatSoundVariant {

    public CatSoundVariantImpl {
        Objects.requireNonNull(adultSounds, "adultSounds");
        Objects.requireNonNull(babySounds, "babySounds");
    }

    record CatSoundSetImpl(
            SoundEvent ambientSound,
            SoundEvent strayAmbientSound,
            SoundEvent hissSound,
            SoundEvent hurtSound,
            SoundEvent deathSound,
            SoundEvent eatSound,
            SoundEvent begForFoodSound,
            SoundEvent purrSound,
            SoundEvent purreowSound
    ) implements CatSoundVariant.CatSoundSet {

        public CatSoundSetImpl {
            Objects.requireNonNull(ambientSound, "ambientSound");
            Objects.requireNonNull(strayAmbientSound, "strayAmbientSound");
            Objects.requireNonNull(hissSound, "hissSound");
            Objects.requireNonNull(hurtSound, "hurtSound");
            Objects.requireNonNull(deathSound, "deathSound");
            Objects.requireNonNull(eatSound, "eatSound");
            Objects.requireNonNull(begForFoodSound, "begForFoodSound");
            Objects.requireNonNull(purrSound, "purrSound");
            Objects.requireNonNull(purreowSound, "purreowSound");
        }
    }

}
