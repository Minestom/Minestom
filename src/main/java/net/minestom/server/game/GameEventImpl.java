package net.minestom.server.game;


import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Represents a game event implementation.
 * Used for a wide variety of events, from weather to bed use to game mode to demo messages.
 */
record GameEventImpl(RegistryData.GameEventEntry registry, Key key, int id) implements GameEvent {
    static final Registry<GameEvent> REGISTRY = RegistryData.createStaticRegistry(
            Key.key("minecraft:game_event"), GameEventImpl::createImpl);

    /**
     * Creates a new {@link GameEventImpl} with the given namespace and properties.
     *
     * @param namespace  the namespace
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

    public static @UnknownNullability GameEvent get(String key) {
        return REGISTRY.get(Key.key(key));
    }

}