package net.minestom.server.utils.collection;

import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;

// Most of the code comes from fastutil's Object2IntOpenHashMap
@ApiStatus.Internal
public final class IndexMap<K> {
    private K[] key;
    private int[] value;
    private int lastIndex;

    private int maxFill, size;
    private final float f = 0.75f;

    public IndexMap() {
        final int n = arraySize(16, f);
        maxFill = maxFill(n, f);
        key = (K[]) new Object[n + 1];
        value = new int[n + 1];
    }

    @Contract(pure = true)
    public int get(@NotNull K key) {
        final int hash = HashCommon.mix(key.hashCode());
        int index = getInt(key, hash);
        if (index == -1) {
            synchronized (this) {
                index = getInt(key, hash);
                if (index == -1) put(key, (index = lastIndex++), hash);
            }
        }
        return index;
    }

    private int getInt(final K k, int hash) {
        final K[] key = this.key;
        final int[] value = this.value;
        if (key.length != value.length)
            return -1; // Race condition, continue to slow path

        final int mask = key.length - 2;
        K curr;
        int pos;
        // The starting point.
        if ((curr = key[pos = hash & mask]) == null) return -1;
        if (k.equals(curr)) return value[pos];
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == null) return -1;
            if (k.equals(curr)) return value[pos];
        }
    }

    private void put(final K k, final int v, int hash) {
        int pos = find(k, hash);
        if (pos >= 0) {
            value[pos] = v;
        } else {
            pos = -(pos + 1);
            key[pos] = k;
            value[pos] = v;
            if (size++ >= maxFill) rehash(arraySize(size + 1, f));
        }
    }

    private int find(final K k, int hash) {
        final K[] key = this.key;
        final int mask = key.length - 2;

        K curr;
        int pos;
        // The starting point.
        if ((curr = key[pos = hash & mask]) == null)
            return -(pos + 1);
        if (k.equals(curr)) return pos;
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == null) return -(pos + 1);
            if (k.equals(curr)) return pos;
        }
    }

    private void rehash(final int newN) {
        final K[] key = this.key;
        final int[] value = this.value;
        final int n = key.length - 1;

        final int mask = newN - 1; // Note that this is used by the hashing macro
        final K[] newKey = (K[]) new Object[newN + 1];
        final int[] newValue = new int[newN + 1];

        int i = n, pos;
        for (int j = size; j-- != 0; ) {
            while (((key[--i]) == null)) ;
            if (!((newKey[pos = HashCommon.mix(key[i].hashCode()) & mask]) == null))
                while (!((newKey[pos = (pos + 1) & mask]) == null)) ;
            newKey[pos] = key[i];
            newValue[pos] = value[i];
        }
        newValue[newN] = value[n];
        this.maxFill = maxFill(n, f);
        this.key = newKey;
        this.value = newValue;
    }
}
