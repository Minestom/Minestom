package net.minestom.server.gameevent;

import java.util.Collection;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record GameEventImpl(Registry.GameEventEntry registry) implements GameEvent {
    private static final Registry.Container<GameEvent> CONTAINER = new Registry.Container<>(
            Registry.Resource.GAMEPLAY_TAGS,
            (container, namespace, object) -> container.register(new GameEventImpl(Registry.gameEvent(namespace, object, null)))
    );

    static @Nullable GameEvent get(final @NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static @Nullable GameEvent getSafe(final @NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static @Nullable GameEvent getId(final int id) {
        return CONTAINER.getId(id);
    }

    static @NotNull Collection<@NotNull GameEvent> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return namespace().toString();
    }
}
