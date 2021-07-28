package net.minestom.server.sound;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

final class SoundEventImpl implements SoundEvent {

    private final NamespaceID namespaceID;
    private final int id;

    SoundEventImpl(NamespaceID namespaceID, int id) {
        this.namespaceID = namespaceID;
        this.id = id;
    }

    @Override
    public @NotNull NamespaceID namespace() {
        return namespaceID;
    }

    @Override
    public int id() {
        return id;
    }
}
