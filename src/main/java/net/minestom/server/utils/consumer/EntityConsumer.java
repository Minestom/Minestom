package net.minestom.server.utils.consumer;

import net.minestom.server.entity.Entity;

@FunctionalInterface
public interface EntityConsumer {
    void accept(Entity entity);
}
