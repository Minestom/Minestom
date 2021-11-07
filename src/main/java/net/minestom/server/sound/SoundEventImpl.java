package net.minestom.server.sound;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class SoundEventImpl implements SoundEvent {
    private static final Registry.Container<SoundEvent> CONTAINER = new Registry.Container<>(Registry.Resource.SOUNDS,
            (container, namespace, object) -> {
                final int id = (int) object.get("id");
                container.register(new SoundEventImpl(NamespaceID.from(namespace), id));
            });

    static SoundEvent get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static SoundEvent getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static SoundEvent getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<SoundEvent> values() {
        return CONTAINER.values();
    }

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

    @Override
    public String toString() {
        return name();
    }
}
