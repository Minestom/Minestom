package net.minestom.server.tags;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public final class GameTag<T extends ProtocolObject> {

    private final NamespaceID name;
    private final GameTagType<T> type;
    private final Collection<T> values;

    public GameTag(final @NotNull NamespaceID name, final @NotNull GameTagType<@NotNull T> type, final @NotNull Set<@NotNull String> keys) {
        this.name = name;
        this.type = type;
        final List<T> values = new ArrayList<>();
        for (final var key : keys) {
            values.add(type.fromName().apply(key));
        }
        this.values = Collections.unmodifiableCollection(values);
    }

    public GameTag(final @NotNull NamespaceID name, final @NotNull GameTagType<@NotNull T> type, final @NotNull Collection<T> values) {
        this.name = name;
        this.type = type;
        this.values = Collections.unmodifiableCollection(values);
    }

    public @NotNull NamespaceID name() {
        return name;
    }

    public @NotNull GameTagType<@NotNull T> type() {
        return type;
    }

    public @NotNull @Unmodifiable Collection<@NotNull T> values() {
        return values;
    }

    public boolean contains(final @NotNull T value) {
        return values().contains(value);
    }
}
