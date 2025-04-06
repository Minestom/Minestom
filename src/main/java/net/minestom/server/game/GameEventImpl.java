package net.minestom.server.game;


import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a game event implementation.
 * Used for a wide variety of events, from weather to bed use to game mode to demo messages.
 */
record GameEventImpl(RegistryData.GameEventEntry registry, Key key, int id) implements GameEvent {
    private static final RegistryData.Container<GameEvent> CONTAINER = RegistryData.createStaticContainer(RegistryData.Resource.GAME_EVENTS, GameEventImpl::createImpl);

    /**
     * Creates a new {@link GameEventImpl} with the given namespace and properties.
     *
     * @param namespace the namespace
     * @param properties the properties
     * @return a new {@link GameEventImpl}
     */
    private static GameEventImpl createImpl(String namespace, RegistryData.Properties properties) {
        return new GameEventImpl(RegistryData.gameEventEntry(namespace, properties));
    }

    /**
     * Creates a new {@link GameEventImpl} with the given registry.
     *
     * @param registry the registry
     */
    private GameEventImpl(RegistryData.GameEventEntry registry) {
        this(registry, registry.key(), registry.main().getInt("id"));
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