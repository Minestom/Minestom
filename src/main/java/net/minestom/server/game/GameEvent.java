package net.minestom.server.game;


import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
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
     * Gets the namespace ID of this game event.
     *
     * @return the namespace ID
     */
    @Override
    @NotNull
    NamespaceID namespace();

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
     * @param namespaceID the namespace ID
     * @return the game event or null if not found
     */
    static @Nullable GameEvent fromNamespaceId(@NotNull String namespaceID) {
        return GameEventImpl.getSafe(namespaceID);
    }

    /**
     * Gets a game event by its namespace ID.
     *
     * @param namespaceID the namespace ID
     * @return the game event or null if not found
     */
    static @Nullable GameEvent fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

}
