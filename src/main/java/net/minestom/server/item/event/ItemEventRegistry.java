package net.minestom.server.item.event;

import net.minestom.server.item.ItemTag;

import java.util.HashMap;

public class ItemEventRegistry<T> {

    private final HashMap<T, ItemEventIdentifierHandler<T>> identifierHandlerHashMap = new HashMap<>();
    private final ItemTag<T> tag;

    ItemEventRegistry(ItemTag<T> tag) {
        this.tag = tag;
    }

    public ItemEventIdentifierHandler<T> identifier(T identifier) {
        if (identifierHandlerHashMap.containsKey(identifier)) return identifierHandlerHashMap.get(identifier);

        ItemEventIdentifierHandler<T> itemEventIdentifierHandler = new ItemEventIdentifierHandler<>(identifier);

        identifierHandlerHashMap.put(identifier, itemEventIdentifierHandler);

        return itemEventIdentifierHandler;
    }
}
