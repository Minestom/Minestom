package net.minestom.server.utils.identified;

import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

/**
 * A simple uniquely identified implementation.
 */
class UniquelyIdentifiedImpl implements UniquelyIdentified {
    private final UUID uuid;

    /**
     * Creates a new uniquely identified instance.
     *
     * @param uuid the uuid
     */
    UniquelyIdentifiedImpl(@NotNull UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
    }

    @Override
    public @NotNull UUID uniqueIdentity() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniquelyIdentified that = (UniquelyIdentified) o;
        return Objects.equals(uuid, that.uniqueIdentity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
