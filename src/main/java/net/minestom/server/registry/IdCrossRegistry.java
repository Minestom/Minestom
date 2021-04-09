package net.minestom.server.registry;

public interface IdCrossRegistry<T extends ShortKeyed> extends Registry<T> {
    T get(short id);

    interface Writable<T extends ShortKeyed> extends IdCrossRegistry<T>, Registry.Writable<T> { }
}
