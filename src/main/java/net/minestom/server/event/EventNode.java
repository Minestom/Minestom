package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventNode<T extends Event> {

    private EventNode() {
    }

    public static EventNode<Event> create() {
        return new EventNode<>();
    }

    public static <T extends Event, E> EventNode<T> conditional(Class<T> eventType,
                                                                Class<E> handlerType,
                                                                Predicate<E> predicate) {
        return new EventNode<>();
    }

    public static <T extends Event> EventNode<T> conditional(Class<T> eventType) {
        return new EventNode<>();
    }

    public static <T extends Event> EventNode<T> unique(Class<T> eventType, EventHandler handler) {
        return new EventNode<>();
    }

    public void addChild(EventNode<? extends T> child) {
    }

    public void addListener(EventListener<? extends T> listener) {
    }

    public <E extends T> void addListener(Class<E> eventClass, Consumer<E> listener) {
    }
}
