package net.minestom.server.gameevent;

import java.util.Collection;

import java.util.Set;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.tags.GameTag;
import net.minestom.server.tags.GameTags;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An event that may be fired by the game at some point to indicate that
 * something has occurred.
 *
 * These are currently only supported and used for tags in Minestom, and serve
 * no other purpose. For events, see the {@link net.minestom.server.event}
 * package, as well as the
 * <a href="https://wiki.minestom.net/feature/events">Wiki page</a>.
 */
public sealed interface GameEvent extends ProtocolObject permits GameEventImpl {

    static @NotNull Collection<@NotNull GameEvent> values() {
        return GameEventImpl.values();
    }

    static @Nullable GameEvent fromNamespaceId(final @NotNull String namespaceId) {
        return GameEventImpl.getSafe(namespaceId);
    }

    static @Nullable GameEvent fromNamespaceId(final @NotNull NamespaceID namespaceId) {
        return fromNamespaceId(namespaceId.asString());
    }

    static @Nullable GameEvent fromId(final int id) {
        return GameEventImpl.getId(id);
    }

    static @NotNull Set<GameTag<GameEvent>> tags() {
        return GameTags.GAME_EVENTS;
    }

    /**
     * Returns the registry entry this game event was created from.
     *
     * @return the registry entry
     */
    @Contract(pure = true)
    @NotNull Registry.GameEventEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    @Override
    default int id() {
        return registry().id();
    }

    /**
     * Gets the radius that a game event dispatcher should search in for
     * listeners to dispatch this game event to.
     *
     * @return the notification radius
     */
    default int notificationRadius() {
        return registry().notificationRadius();
    }
}
