package net.minestom.server.item.event;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.ItemTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Static ItemEvent object. Allows for registering events on items based on NBT.
 * <br />
 * <br />
 * Made up of an ItemEventRegistry and an ItemEventIdentifierHandler.
 * <br />
 * <br />
 * An ItemTag is passed to check (ex ItemTag.String("test"))
 * <br />
 * And an identifier is used as the value to check in that ItemTag.
 */
public class ItemEvents {

    private static final ConcurrentHashMap<ItemTag<?>, ItemEventRegistry> events = new ConcurrentHashMap<>();

    private ItemEvents() {

    }

    /**
     * Gets an existing ItemEventRegistry, or creates one if none was found.
     *
     * @param tag The tag used to find the ItemEventRegistry. Used for general identification.
     * @param <T> The type of Identifier required for the ItemTag
     *
     * @return An ItemEventRegistry instance. Will create a new one if none were found.
     */
    public static <T> @NotNull ItemEventRegistry<T> registryOrNew(@NotNull ItemTag<T> tag) {
        return events.computeIfAbsent(tag, key -> new ItemEventRegistry<>());
    }

    /**
     * Gets an existing ItemEventRegistry, returns null if none found.
     *
     * @param tag The tag used to find the ItemEventRegistry. Used for general identification.
     * @param <T> The type of Identifier required for the ItemTag
     *
     * @return An ItemEventRegistry instance. Will return null if not found.
     */
    public static <T> @Nullable ItemEventRegistry<T> registry(@NotNull ItemTag<T> tag) {
        return events.get(tag);
    }

    /**
     * Attempts to call an event on an item.
     *
     * @param itemStack The item to call the event on.
     * @param eventClass The class of the event to be called.
     * @param event The event object to pass to the event callback.
     * @param <E> The event type.
     *
     * @return If the event was cancelled or not.
     */
    public static <E extends Event> boolean callEventOnItem(@NotNull ItemStack itemStack, @NotNull Class<E> eventClass, @NotNull E event) {

        boolean cancelled = false;

        for (ItemTag<?> tag : events.keySet()) {

            // If the tag was not found on the item then go to the next loop
            if (itemStack.getMeta().get(tag) == null) {
                continue;
            }

            // If the value of this tag is not registered as an event identifier handler, continue
            if (events.get(tag).identifier(itemStack.getMeta().get(tag)) == null) {
                continue;
            }

            // Call the event
            events.get(tag).identifier(itemStack.getMeta().get(tag)).callEvent(eventClass, event);

            // If the event is cancellable, then set the boolean "cancelled" value to the cancelled result.
            if (event instanceof CancellableEvent) {
                CancellableEvent cancellableEvent = (CancellableEvent) event;
                cancelled = cancellableEvent.isCancelled();
            }

        }

        return cancelled;
    }

}
