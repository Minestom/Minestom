package net.minestom.server.tags;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public record GameTag<T extends ProtocolObject>(NamespaceID name,
                                                GameTagType<T> type,
                                                Collection<T> values) {
    public GameTag {
        values = List.copyOf(values);
    }

    public GameTag(final @NotNull NamespaceID name, final @NotNull GameTagType<@NotNull T> type, final @NotNull Set<@NotNull String> keys) {
        this(name, type, keys.stream().map(key -> type.fromName().apply(key)).toList());
    }

    public boolean contains(final @NotNull T value) {
        return values().contains(value);
    }
}
