package net.minestom.server.instance;

import org.jetbrains.annotations.NotNull;

public enum ChunkStatus {
    INITIALIZATION,
    READING,
    GENERATION,
    POPULATION,
    LIGHTING,
    COMPLETE;

    public boolean isOrAfter(@NotNull ChunkStatus status) {
        return ordinal() >= status.ordinal();
    }
}
