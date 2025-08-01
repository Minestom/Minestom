package net.minestom.server.instance;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

record BlockVersionImpl(long instance,
                        // Null if global
                        @Nullable Long2ObjectOpenHashMap<ChunkVersion> chunkVersions
) implements BlockVersion {
    @Override
    public boolean global() {
        return chunkVersions == null;
    }

    @Override
    public boolean compatible(BlockVersion other) {
        BlockVersionImpl impl = (BlockVersionImpl) other;
        if (instance == impl.instance) return true;
        if (global() || impl.global()) return false;
        return chunkVersions.equals(impl.chunkVersions);
    }

    record ChunkVersion(long chunk, Long2LongOpenHashMap sectionVersions) {
    }
}
