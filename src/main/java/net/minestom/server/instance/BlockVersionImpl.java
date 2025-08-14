package net.minestom.server.instance;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.jetbrains.annotations.Nullable;

record BlockVersionImpl(long instance,
                        // Null if global
                        @Nullable Long2LongOpenHashMap sectionVersions
) implements BlockVersion {
    @Override
    public boolean global() {
        return sectionVersions == null;
    }

    @Override
    public boolean compatible(BlockVersion other) {
        BlockVersionImpl impl = (BlockVersionImpl) other;
        if (instance == impl.instance) return true;
        if (global() || impl.global()) return false;
        for (Long2LongMap.Entry entry : sectionVersions.long2LongEntrySet()) {
            final long section = entry.getLongKey();
            final long version = entry.getLongValue();
            if (impl.sectionVersions.get(section) != version) {
                return false;
            }
        }
        return true;
    }
}
