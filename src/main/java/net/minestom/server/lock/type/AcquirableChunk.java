package net.minestom.server.lock.type;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

public class AcquirableChunk extends AcquirableImpl<Chunk> {
    public AcquirableChunk(@NotNull Chunk value) {
        super(value);
    }
}
