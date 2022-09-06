package net.minestom.server.utils.collection;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class AutoIncrementMap<K> {
    private final Object2IntOpenHashMap<K> write = new Object2IntOpenHashMap<>();
    private Object2IntOpenHashMap<K> read;
    private int lastIndex;

    public AutoIncrementMap() {
        this.write.defaultReturnValue(-1);
        this.read = write.clone();
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
                    read = write.clone();
                }
            }
        }
        return index;
    }
}
