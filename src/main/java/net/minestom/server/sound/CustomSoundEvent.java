package net.minestom.server.sound;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record CustomSoundEvent(@NotNull NamespaceID namespace, @Nullable Float range) implements SoundEvent {
}
