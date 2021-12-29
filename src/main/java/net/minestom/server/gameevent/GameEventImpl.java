package net.minestom.server.gameevent;

import net.minestom.server.registry.Registry;
import net.minestom.server.tags.GameTag;
import net.minestom.server.tags.GameTags;
import net.minestom.server.tags.GameTagType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

record GameEventImpl(Registry.GameEventEntry registry) implements GameEvent {
    private static final Registry.Container<GameEvent> CONTAINER = new Registry.Container<>(
            Registry.Resource.GAME_EVENTS,
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
        return name();
    }
}
