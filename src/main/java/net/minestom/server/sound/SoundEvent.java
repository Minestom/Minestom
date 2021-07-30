package net.minestom.server.sound;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@ApiStatus.NonExtendable
public interface SoundEvent extends ProtocolObject, SoundEventConstants {

    static @NotNull Collection<@NotNull SoundEvent> values() {
        return SoundEventImpl.values();
    }

    static @Nullable SoundEvent fromNamespaceId(@NotNull String namespaceID) {
        return SoundEventImpl.get(namespaceID);
    }

    static @Nullable SoundEvent fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable SoundEvent fromId(int id) {
        return SoundEventImpl.getId(id);
    }
}
