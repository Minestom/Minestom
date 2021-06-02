package net.minestom.server.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface EventFilter<E extends Event, H> {

    EventFilter<Event, ?> ALL = from(Event.class);
    EventFilter<EntityEvent, Entity> ENTITY = from(EntityEvent.class, Entity.class, EntityEvent::getEntity);
    EventFilter<PlayerEvent, Player> PLAYER = from(PlayerEvent.class, Player.class, PlayerEvent::getPlayer);
    EventFilter<ItemEvent, ItemStack> ITEM = from(ItemEvent.class, ItemStack.class, ItemEvent::getItemStack);
    EventFilter<InstanceEvent, Instance> INSTANCE = from(InstanceEvent.class, Instance.class, InstanceEvent::getInstance);
    EventFilter<InventoryEvent, Inventory> INVENTORY = from(InventoryEvent.class, Inventory.class, InventoryEvent::getInventory);

    static <E extends Event, H> EventFilter<E, H> from(@NotNull Class<E> eventType,
                                                       @Nullable Class<H> handlerType,
                                                       @Nullable Function<E, H> handlerGetter) {
        return new EventFilter<>() {
            @Override
            public @Nullable H getHandler(@NotNull E event) {
                return handlerGetter != null ? handlerGetter.apply(event) : null;
            }

            @Override
            public @NotNull Class<E> getEventType() {
                return eventType;
            }
        };
    }

    static <E extends Event, H> EventFilter<E, H> from(@NotNull Class<E> type) {
        return from(type, null, null);
    }

    @Nullable H getHandler(@NotNull E event);

    @NotNull Class<E> getEventType();
}
