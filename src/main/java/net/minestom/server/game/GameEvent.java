package net.minestom.server.game;


import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Represents a game event.
 * Used for a wide variety of events, from weather to bed use to game mode to demo messages.
 */
public sealed interface GameEvent extends StaticProtocolObject permits GameEventImpl {

    /**
     * Returns the game event registry.
     *
     * @return the game event registry or null if not found
     */
    @Contract(pure = true)
    @Nullable
    Registry.GameEventEntry registry();

    /**
     * Gets the game events from the registry.
     *
     * @return the game events
     */
    static @NotNull Collection<@NotNull GameEvent> values() {
        return GameEventImpl.values();
    }

    /**
     * Gets a game event by its namespace ID.
     *
     * @param key the namespace ID
     * @return the game event or null if not found
     */
    static @Nullable GameEvent fromKey(@NotNull String key) {
        return GameEventImpl.getSafe(key);
    }

    /**
     * Gets a game event by its namespace ID.
     *
     * @param key the event key
     * @return the game event or null if not found
     */
    static @Nullable GameEvent fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

}
