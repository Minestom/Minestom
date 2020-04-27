package net.minestom.server.event;

public interface EventCallback<E extends Event> {

    void run(E event);

}
