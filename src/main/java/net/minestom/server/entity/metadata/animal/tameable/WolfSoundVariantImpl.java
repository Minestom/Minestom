package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

record WolfSoundVariantImpl(
        @NotNull SoundEvent ambientSound,
        @NotNull SoundEvent deathSound,
        @NotNull SoundEvent growlSound,
        @NotNull SoundEvent hurtSound,
        @NotNull SoundEvent pantSound,
        @NotNull SoundEvent whineSound
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
