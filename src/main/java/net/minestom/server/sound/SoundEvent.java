package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface SoundEvent extends ProtocolObject, Sound.Type, SoundEvents permits SoundEventImpl {

    static @NotNull Collection<@NotNull SoundEvent> values() {
        return SoundEventImpl.values();
    }

    static @Nullable SoundEvent fromNamespaceId(@NotNull String namespaceID) {
        return SoundEventImpl.getSafe(namespaceID);
    }

    static @Nullable SoundEvent fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable SoundEvent fromId(int id) {
        return SoundEventImpl.getId(id);
    }

    @Override
    default @NotNull Key key() {
        return ProtocolObject.super.key();
    }
}
