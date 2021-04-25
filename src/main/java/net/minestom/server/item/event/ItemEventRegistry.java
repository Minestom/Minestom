package net.minestom.server.item.event;

import net.minestom.server.item.ItemTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ItemEventRegistry<T> {

    private final HashMap<T, ItemEventIdentifierHandler<T>> identifierHandlerHashMap = new HashMap<>();
    private final ItemTag<T> tag;

    ItemEventRegistry(ItemTag<T> tag) {
        this.tag = tag;
    }

    public @NotNull ItemEventIdentifierHandler<T> identifierOrNew(T identifier) {
        if (identifierHandlerHashMap.containsKey(identifier)) return identifierHandlerHashMap.get(identifier);

        ItemEventIdentifierHandler<T> itemEventIdentifierHandler = new ItemEventIdentifierHandler<>(identifier);

        identifierHandlerHashMap.put(identifier, itemEventIdentifierHandler);

        return itemEventIdentifierHandler;
    }

    public @Nullable ItemEventIdentifierHandler<T> identifier(T identifier) {
        return identifierHandlerHashMap.get(identifier);
    }
}
