package net.minestom.server.item.event;

import net.minestom.server.item.ItemTag;

import java.util.concurrent.ConcurrentHashMap;

public class ItemEvents {

    private static final ConcurrentHashMap<ItemTag, ItemEventRegistry> events = new ConcurrentHashMap<>();

    private ItemEvents() {

    }

    public static <T> ItemEventRegistry<T> registry(ItemTag<T> tag) {
        if (events.containsKey(tag)) return events.get(tag);

        ItemEventRegistry<T> handler = new ItemEventRegistry<T>(tag);

        events.put(tag, handler);

        return handler;
    }

}
