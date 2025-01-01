package net.minestom.server.property;

public interface Property<T> {
    String of(T value);
    String id();
}
