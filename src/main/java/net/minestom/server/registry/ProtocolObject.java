package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ProtocolObject extends Keyed {

    /**
     * A set of some protocol objects. May contain a single element, multiple elements, or a single tag (which itself contains multiple elements).
     *
     * @param <T> The type of protocol object represented by this set.
     */
    interface Set<T extends ProtocolObject> {



    }

    @Contract(pure = true)
    @NotNull NamespaceID namespace();

    @Contract(pure = true)
    default @NotNull String name() {
        return namespace().asString();
    }

    @Override
    @Contract(pure = true)
    default @NotNull Key key() {
        return namespace();
    }

    default @Nullable Object registry() {
        return null;
    }
}
