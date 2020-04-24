package net.minestom.server.event;

public interface Callback<E extends Event> {

    void run(E event);

}
