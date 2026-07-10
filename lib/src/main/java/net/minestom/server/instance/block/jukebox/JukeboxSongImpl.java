package net.minestom.server.instance.block.jukebox;

import net.kyori.adventure.text.Component;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.validate.Check;

record JukeboxSongImpl(
        SoundEvent soundEvent,
        Component description,
        float lengthInSeconds,
        int comparatorOutput
) implements JukeboxSong {

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    JukeboxSongImpl {
        Check.argCondition(soundEvent == null, "missing sound event");
        Check.argCondition(description == null, "missing description");
    }

}
