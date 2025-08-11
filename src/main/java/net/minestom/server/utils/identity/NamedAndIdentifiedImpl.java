package net.minestom.server.utils.identity;

import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Simple implementation of {@link NamedAndIdentified}.
 *
 * @see #of(String, UUID)
 * @see #of(Component, UUID)
 */
class NamedAndIdentifiedImpl implements NamedAndIdentified {
    private final Component name;
    private final UUID uuid;

    /**
     * Creates a new named and identified implementation.
     *
     * @param name the name
     * @param uuid the uuid
     * @see NamedAndIdentified#of(String, UUID)
     */
    NamedAndIdentifiedImpl(String name, UUID uuid) {
        this(Component.text(name), uuid);
    }

    /**
     * Creates a new named and identified implementation.
     *
     * @param name the name
     * @param uuid the uuid
     * @see NamedAndIdentified#of(Component, UUID)
     */
    NamedAndIdentifiedImpl(Component name, UUID uuid) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.uuid = Objects.requireNonNull(uuid, "uuid cannot be null");
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamedAndIdentified that)) return false;
        return this.uuid.equals(that.getUuid());
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
