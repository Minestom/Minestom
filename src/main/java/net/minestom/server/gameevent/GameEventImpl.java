package net.minestom.server.gameevent;

import java.util.Collection;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

final class GameEventImpl implements GameEvent {
    private static final Registry.Container<GameEvent> CONTAINER = new Registry.Container<>(Registry.Resource.GAME_EVENTS,
            (container, namespace, object) -> container.register(new GameEventImpl(Registry.gameEvent(namespace, object, null))));

    static GameEvent get(final @NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static GameEvent getSafe(final @NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static GameEvent getId(final int id) {
        return CONTAINER.getId(id);
    }

    static Collection<GameEvent> values() {
        return CONTAINER.values();
    }

    private final Registry.GameEventEntry registry;

    GameEventImpl(final Registry.GameEventEntry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Registry.GameEventEntry registry() {
        return registry;
    }

    @Override
    public String toString() {
        return namespace().asString();
    }
}
