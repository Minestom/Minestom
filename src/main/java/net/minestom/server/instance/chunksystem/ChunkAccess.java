package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * <b>For internal use only, may be changed at any time. Do not use this.</b>
 * <p>
 * An interface for the chunk system to access protected methods of some classes like {@link Chunk}.
 * Probably going to get changed later, I just want to try to modify the least amount of existing code at this time. 
 */
@ApiStatus.Internal
public interface ChunkAccess {
    void onLoad(Chunk chunk);

    void unload(@NotNull Chunk chunk);
}
