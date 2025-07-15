package net.minestom.server.game;


import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Represents a game event.
 * Used for a wide variety of events, from weather to bed use to game mode to demo messages.
 */
public sealed interface GameEvent extends StaticProtocolObject<GameEvent> permits GameEventImpl {

    /**
     * Returns the game event registry.
     *
     * @return the game event registry or null if not found
     */
    @Contract(pure = true)
    RegistryData.@Nullable GameEventEntry registry();

    static Collection<GameEvent> values() {
        return GameEventImpl.REGISTRY.values();
    }

    static @Nullable GameEvent fromKey(@KeyPattern String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable GameEvent fromKey(Key key) {
        return GameEventImpl.REGISTRY.get(key);
    }

    static @Nullable GameEvent fromId(int id) {
        return GameEventImpl.REGISTRY.get(id);
    }

    static Registry<GameEvent> staticRegistry() {
        return GameEventImpl.REGISTRY;
    }

}
