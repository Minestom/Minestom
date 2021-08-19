package net.minestom.server.gameevent;

import java.util.Collection;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface GameEvent extends ProtocolObject {

    static @NotNull Collection<@NotNull GameEvent> values() {
        return GameEventImpl.values();
    }

    static @Nullable GameEvent fromNamespaceId(final @NotNull String namespaceID) {
        return GameEventImpl.getSafe(namespaceID);
    }

    static @Nullable GameEvent fromNamespaceId(final @NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable GameEvent fromId(final int id) {
        return GameEventImpl.getId(id);
    }

    /**
     * Returns the game event registry.
     *
     * @return the game event registry
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

    default int notificationRadius() {
        return registry().notificationRadius();
    }
}
