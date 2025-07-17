package net.minestom.server.game;


import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @Nullable
    RegistryData.GameEventEntry registry();

    static @NotNull Collection<@NotNull GameEvent> values() {
        return GameEventImpl.REGISTRY.values();
    }

    static @Nullable GameEvent fromKey(@KeyPattern @NotNull String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable GameEvent fromKey(@NotNull Key key) {
        return GameEventImpl.REGISTRY.get(key);
    }

    static @Nullable GameEvent fromId(int id) {
        return GameEventImpl.REGISTRY.get(id);
    }

    static @NotNull Registry<GameEvent> staticRegistry() {
        return GameEventImpl.REGISTRY;
    }

}
