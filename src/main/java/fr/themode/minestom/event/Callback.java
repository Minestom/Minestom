package fr.themode.minestom.event;

public interface Callback<E extends Event> {

    void run(E event);

}
