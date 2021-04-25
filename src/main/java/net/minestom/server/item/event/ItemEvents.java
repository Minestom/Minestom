package net.minestom.server.item.event;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.ItemTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class ItemEvents {

    private static final ConcurrentHashMap<ItemTag, ItemEventRegistry> events = new ConcurrentHashMap<>();

    private ItemEvents() {

    }

    public static <T> @NotNull ItemEventRegistry<T> getRegistryOrNew(ItemTag<T> tag) {
        if (events.containsKey(tag)) return events.get(tag);

        ItemEventRegistry<T> handler = new ItemEventRegistry<T>(tag);

        events.put(tag, handler);

        return handler;
    }

    public static <T> @Nullable ItemEventRegistry<T> getRegistry(ItemTag<T> tag) {
        return events.get(tag);
    }

    public static <E extends Event> boolean attemptEventCalls(ItemStack itemStack, Class<E> eventClass, E event) {

        boolean cancelled = false;

        for (ItemTag tag : events.keySet()) {
            if (itemStack.getMeta().get(tag) == null) continue;

            if (events.get(tag).identifier(itemStack.getMeta().get(tag)) == null) continue;

            else events.get(tag).identifier(itemStack.getMeta().get(tag)).callEvent(eventClass, event);

            if (event instanceof CancellableEvent) {
                CancellableEvent cancellableEvent = (CancellableEvent) event;
                cancelled = cancellableEvent.isCancelled();
            }

        }

        return cancelled;
    }

}
