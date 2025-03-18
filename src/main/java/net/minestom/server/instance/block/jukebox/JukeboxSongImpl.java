package net.minestom.server.instance.block.jukebox;

import net.kyori.adventure.text.Component;
import net.minestom.server.registry.Registry;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record JukeboxSongImpl(
        @NotNull SoundEvent soundEvent,
        @NotNull Component description,
        float lengthInSeconds,
        int comparatorOutput,
        @Nullable Registry.JukeboxSongEntry registry
) implements JukeboxSong {

    @SuppressWarnings("ConstantValue") // The builder can violate the nullability constraints
    JukeboxSongImpl {
        Check.argCondition(soundEvent == null, "missing sound event");
        Check.argCondition(description == null, "missing description");
    }

    JukeboxSongImpl(@NotNull Registry.JukeboxSongEntry registry) {
        this(registry.soundEvent(), registry.description(), registry.lengthInSeconds(), registry.comparatorOutput(), registry);
    }

}
