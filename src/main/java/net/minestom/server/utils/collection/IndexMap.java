package net.minestom.server.utils.collection;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class IndexMap<K> {
    private final Object2IntOpenHashMap<K> write = new Object2IntOpenHashMap<>();
    private Object2IntOpenHashMap<K> read = copy();
    private int lastIndex;

    public IndexMap() {
        write.defaultReturnValue(-1);
    }

    @Contract(pure = true)
    public int get(@NotNull K key) {
        int index = read.getInt(key);
        if (index == -1) {
            synchronized (write) {
                var write = this.write;
                index = write.getInt(key);
                if (index == -1) {
                    write.put(key, (index = lastIndex++));
                    read = copy();
                }
            }
        }
        return index;
    }

    private Object2IntOpenHashMap<K> copy() {
        var map = new Object2IntOpenHashMap<>(write);
        map.defaultReturnValue(-1);
        return map;
    }
}
