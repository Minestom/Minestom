package net.minestom.server.snapshot;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents the complete state of the server at a given moment.
 */
public non-sealed interface ServerSnapshot extends Snapshot {
    @NotNull List<@NotNull InstanceSnapshot> instances();
}
