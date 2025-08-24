package net.minestom.server.game;


import net.kyori.adventure.key.Key;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Represents a game event implementation.
 * Used for a wide variety of events, from weather to bed use to game mode to demo messages.
 */
record GameEventImpl(RegistryData.GameEventEntry registry) implements GameEvent {
    static final Registry<GameEvent> REGISTRY = RegistryData.createStaticRegistry(
            BuiltinRegistries.GAME_EVENT, GameEventImpl::createImpl);

    /**
     * Creates a new {@link GameEventImpl} with the given namespace and properties.
     *
     * @param namespace  the namespace
     * @param properties the properties
     * @return a new {@link GameEventImpl}
     */
    private static GameEventImpl createImpl(RegistryKey<GameEvent> namespace, RegistryData.Properties properties) {
        return new GameEventImpl(RegistryData.gameEventEntry(namespace, properties));
    }

    static @UnknownNullability GameEvent get(RegistryKey<GameEvent> key) {
        return REGISTRY.get(key);
    }

    @Override
    public Key key() {
        return registry.key();
    }

    @Override
    public int id() {
        return registry.id();
    }

    @Override
    public int notificationRadius() {
        return registry.notificationRadius();
    }
}