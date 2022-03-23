package net.minestom.server.utils.collection;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class IndexMap<T> {
    private final Object2IntOpenHashMap<T> indexMap;
    private int lastIndex = 0; // Synchronized on INDEX_MAP

    public IndexMap() {
        this.indexMap = new Object2IntOpenHashMap<>();
        indexMap.defaultReturnValue(-1);
    }

    public int get(@NotNull T key) {
        int index = indexMap.getInt(key);
        if (index == -1) {
            synchronized (indexMap) {
                index = indexMap.getInt(key);
                if (index == -1) {
                    index = lastIndex++;
                    indexMap.put(key, index);
                }
            }
        }
        return index;
    }
}
