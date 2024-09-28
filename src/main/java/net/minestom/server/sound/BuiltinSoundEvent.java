package net.minestom.server.sound;

import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record BuiltinSoundEvent(NamespaceID namespace, int id) implements StaticProtocolObject, SoundEvent {
    private static final Registry.Container<BuiltinSoundEvent> CONTAINER = Registry.createStaticContainer(
            Registry.loadRegistry(Registry.Resource.SOUNDS, Registry.SoundEntry::new).stream()
                    .map(soundEntry -> new BuiltinSoundEvent(soundEntry.namespace(), soundEntry.id())).toList());

    static SoundEvent get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static SoundEvent getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static SoundEvent getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<? extends SoundEvent> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
