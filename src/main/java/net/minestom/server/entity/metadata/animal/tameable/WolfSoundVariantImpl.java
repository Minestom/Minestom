package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.validate.Check;

record WolfSoundVariantImpl(
        SoundEvent ambientSound,
        SoundEvent deathSound,
        SoundEvent growlSound,
        SoundEvent hurtSound,
        SoundEvent pantSound,
        SoundEvent whineSound
) implements WolfSoundVariant {

    public WolfSoundVariantImpl {
        Check.notNull(ambientSound, "Ambient sound");
        Check.notNull(deathSound, "Death sound");
        Check.notNull(growlSound, "Growl sound");
        Check.notNull(hurtSound, "Hurt sound");
        Check.notNull(pantSound, "Pant sound");
        Check.notNull(whineSound, "Whine sound");
    }

}
