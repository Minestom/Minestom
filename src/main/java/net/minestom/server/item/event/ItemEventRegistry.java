package net.minestom.server.item.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ItemEventRegistry<T> {

    private final HashMap<T, ItemEventIdentifierHandler<T>> identifierHandlerHashMap = new HashMap<>();

    ItemEventRegistry() {

    }

    /**
     * Finds an ItemEventIdentifierHandler by the identifier value, and creates a new instance if none were found.
     *
     * @param identifier The identifier used to find the ItemEventIdentifierHandler
     *
     * @return The found ItemEventIdentifierHandler with a new instance if none were found.
     */
    public @NotNull ItemEventIdentifierHandler<T> identifierOrNew(@Nullable T identifier) {
        return identifierHandlerHashMap.computeIfAbsent(identifier, key -> new ItemEventIdentifierHandler<>());
    }

    /**
     * Finds an ItemEventIdentifierHandler by the identifier value
     *
     * @param identifier The identifier used to find the ItemEventIdentifierHandler
     *
     * @return The found ItemEventIdentifierHandler, null of none
     */
    public @Nullable ItemEventIdentifierHandler<T> identifier(@Nullable T identifier) {
        return identifierHandlerHashMap.get(identifier);
    }
}
