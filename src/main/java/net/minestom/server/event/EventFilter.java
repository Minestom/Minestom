package net.minestom.server.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.handler.EventHandler;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface EventFilter<E extends Event, H extends EventHandler> {

    EventFilter<Event, EventHandler> ALL = from(Event.class);
    EventFilter<EntityEvent, Entity> ENTITY = from(EntityEvent.class, Entity.class, EntityEvent::getEntity);
    EventFilter<PlayerEvent, Player> PLAYER = from(PlayerEvent.class, Player.class, PlayerEvent::getPlayer);

    static <E extends Event, H extends EventHandler> EventFilter<E, H> from(@NotNull Class<E> eventType,
                                                                            @NotNull Class<H> handlerType,
                                                                            @NotNull Function<E, H> handlerGetter) {
        return new EventFilter<>() {
            @Override
            public @Nullable H getHandler(@NotNull E event) {
                return handlerGetter.apply(event);
            }

            @Override
            public @NotNull Class<E> getEventType() {
                return eventType;
            }
        };
    }

    static <E extends Event, H extends EventHandler> EventFilter<E, H> from(@NotNull Class<E> type) {
        return new EventFilter<>() {
            @Override
            public @Nullable H getHandler(@NotNull E event) {
                return null;
            }

            @Override
            public @NotNull Class<E> getEventType() {
                return type;
            }
        };
    }

    @Nullable H getHandler(@NotNull E event);

    @NotNull Class<E> getEventType();
}
