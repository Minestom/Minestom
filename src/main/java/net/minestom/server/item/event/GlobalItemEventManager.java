package net.minestom.server.item.event;

import java.util.concurrent.ConcurrentHashMap;

public class GlobalItemEventManager {

    private static final ConcurrentHashMap<String, ItemEventHandler> events = new ConcurrentHashMap<>();

    public GlobalItemEventManager() {

    }

    public ItemEventHandler get(String id) {
        if (events.containsKey(id)) return events.get(id);

        ItemEventHandler handler = new ItemEventHandler(id);

        events.put(id, handler);

        return handler;
    }

}
