package net.minestom.server.tags;

import net.minestom.server.registry.ProtocolObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface GameTagHolder<T extends ProtocolObject> {
    @NotNull GameTagType<T> tagType();

    @NotNull Set<@NotNull GameTag<T>> tags();
}
