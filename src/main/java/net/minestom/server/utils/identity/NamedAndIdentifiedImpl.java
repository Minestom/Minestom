package net.minestom.server.utils.identity;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Simple implementation of {@link NamedAndIdentified}.
 */
class NamedAndIdentifiedImpl implements NamedAndIdentified {
    static final Component EMPTY = Component.empty();

    private final Component name;
    private final UUID uuid;

    /**
     * Creates a new named and identified implementation.
     *
     * @param name the name
     * @param uuid the uuid
     * @see NamedAndIdentified#of(Component, UUID)
     */
    NamedAndIdentifiedImpl(@NotNull Component name, @NotNull UUID uuid) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.uuid = Objects.requireNonNull(uuid, "uuid cannot be null");
    }

    @Override
    public @NotNull Component getName() {
        return this.name;
    }

    @Override
    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedAndIdentifiedImpl that = (NamedAndIdentifiedImpl) o;
        return this.uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    @Override
    public String toString() {
        return String.format("NamedAndIdentifiedImpl{name='%s', uuid=%s}", this.name, this.uuid);
    }
}
