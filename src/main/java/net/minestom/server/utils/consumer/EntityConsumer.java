package net.minestom.server.utils.consumer;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EntityConsumer {
    void accept(@NotNull Entity entity);
}
