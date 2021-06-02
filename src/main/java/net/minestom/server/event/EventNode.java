package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import net.minestom.server.event.trait.EventTrait;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventNode<T extends EventTrait> {

    private EventNode() {
    }

    public static EventNode<?> create() {
        return null;
    }

    public static <T extends EventTrait, E> EventNode<T> conditional(Class<T> eventType,
                                                                     Class<E> handlerType,
                                                                     Predicate<E> predicate) {
        return new EventNode<>();
    }

    public static <T extends EventTrait> EventNode<T> conditional(Class<T> eventType) {
        return new EventNode<>();
    }

    public static <T extends EventTrait> EventNode<T> unique(Class<T> eventType, EventHandler handler) {
        return new EventNode<>();
    }

    public void addListener(EventListener<? extends T> listener) {
    }

    public <E extends T> void addListener(Class<E> eventClass, Consumer<E> listener) {
    }
}
