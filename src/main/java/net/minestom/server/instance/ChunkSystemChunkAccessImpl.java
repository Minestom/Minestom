package net.minestom.server.instance;

import net.minestom.server.instance.chunksystem.ChunkAccess;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * <b>For internal use only, may be changed at any time. Do not use this.</b>
 */
@ApiStatus.Internal
public final class ChunkSystemChunkAccessImpl implements ChunkAccess {
    public static final ChunkSystemChunkAccessImpl INSTANCE = new ChunkSystemChunkAccessImpl();

    private ChunkSystemChunkAccessImpl() {
    }

    @Override
    public void onLoad(Chunk chunk) {
        chunk.onLoad();
    }

    @Override
    public void unload(@NotNull Chunk chunk) {
        chunk.unload();
    }
}
