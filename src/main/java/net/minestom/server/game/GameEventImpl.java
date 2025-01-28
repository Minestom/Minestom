package net.minestom.server.game;


import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a game event implementation.
 * Used for a wide variety of events, from weather to bed use to game mode to demo messages.
 */
record GameEventImpl(Registry.GameEventEntry registry, NamespaceID namespace, int id) implements GameEvent {
    private static final Registry.Container<GameEvent> CONTAINER = Registry.createStaticContainer(Registry.Resource.GAME_EVENTS, GameEventImpl::createImpl);

    /**
     * Creates a new {@link GameEventImpl} with the given namespace and properties.
     *
     * @param namespace the namespace
     * @param properties the properties
     * @return a new {@link GameEventImpl}
     */
    private static GameEventImpl createImpl(String namespace, Registry.Properties properties) {
        return new GameEventImpl(Registry.gameEventEntry(namespace, properties));
    }

    /**
     * Creates a new {@link GameEventImpl} with the given registry.
     *
     * @param registry the registry
     */
    private GameEventImpl(Registry.GameEventEntry registry) {
        this(registry, registry.namespace(), registry.main().getInt("id"));
    }

    /**
     * Gets the game events from the registry.
     *
     * @return the game events
     */
    static Collection<GameEvent> values() {
        return CONTAINER.values();
    }

    /**
     * Gets a game event by its namespace ID.
     *
     * @param namespace the namespace ID
     * @return the game event or null if not found
     */
    public static GameEvent get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    /**
     * Gets a game event by its namespace ID.
     *
     * @param namespace the namespace ID
     * @return the game event or null if not found
     */
    static GameEvent getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }
}