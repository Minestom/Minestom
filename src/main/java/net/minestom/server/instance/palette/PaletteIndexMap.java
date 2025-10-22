/*
 * Copyright (C) 2002-2024 Sebastiano Vigna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.HashCommon;
import net.minestom.server.utils.MathUtils;

/**
 * A performant implementation for transforming values into palette indices.
 * Derived from {@link it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap} and modified to suit our specific needs.
 */
@SuppressWarnings({"StatementWithEmptyBody", "UnusedReturnValue"})
final class PaletteIndexMap implements Cloneable {
    private int[] value;
    private int[] index;
    private int[] indexToValue;

    private int mask;
    private boolean containsNullKey;
    private int n;
    private int maxFill;
    private int size;

    /**
     * Assumes bitsPerEntry > 0
     */
    PaletteIndexMap(byte bitsPerEntry) {
        n = 2 << bitsPerEntry;
        mask = n - 1;
        maxFill = n - (n >> 2);
        value = new int[n + 1];
        index = new int[n + 1];
        indexToValue = new int[1 << bitsPerEntry];
    }

    PaletteIndexMap(int[] palette) {
        this(palette.length > 1 ? (byte) MathUtils.bitsToRepresent(palette.length) : 1);
        for (final int value : palette) {
            final int pos = find(value);
            if (pos >= 0) throw new IllegalArgumentException("Palette entries must be unique");
            UNSAFE_insert(~pos, value);
        }
    }

    int valueToIndex(final int value) {
        final int pos = find(value);
        if (pos >= 0) return index[pos];
        return UNSAFE_insert(~pos, value);
    }

    int valueToIndexOrDefault(final int value) {
        final int pos = find(value);
        if (pos >= 0) return index[pos];
        return -1;
    }

    int valueToIndexCapped(final int value, final int maxSize) {
        final int pos = find(value);
        if (pos >= 0) return index[pos];
        if (size >= maxSize) return -1;
        return UNSAFE_insert(~pos, value);
    }

    int indexToValue(final int index) {
        return indexToValue[index];
    }

    /// Should not be modified
    int[] indexToValueArray() {
        return indexToValue;
    }

    int size() {
        return size;
    }

    int maxValue() {
        int result = 0;
        for (int index = 0; index < size; index++) {
            final int value = indexToValue[index];
            if (value > result) result = value;
        }
        return result;
    }

    int find(final int value) {
        if (value == 0) return containsNullKey ? n : ~n;
        int curr;
        final int[] key = this.value;
        int pos;
        // The starting point.
        if ((curr = key[pos = (HashCommon.mix((value))) & mask]) == 0) return ~pos;
        if (value == curr) return pos;
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == 0) return ~pos;
            if (value == curr) return pos;
        }
    }

    int UNSAFE_insert(final int pos, final int value) {
        final int nextIndex = size;
        if (pos == n) containsNullKey = true;
        this.value[pos] = value;
        this.index[pos] = nextIndex;
        if (indexToValue.length == nextIndex) {
            final int[] newIndexToValue = new int[indexToValue.length << 1];
            System.arraycopy(indexToValue, 0, newIndexToValue, 0, indexToValue.length);
            indexToValue = newIndexToValue;
        }
        indexToValue[nextIndex] = value;
        if (++size > maxFill) rehash(n << 1);
        return nextIndex;
    }

    int UNSAFE_getIndex(final int pos) {
        return index[pos];
    }

    void UNSAFE_replace(final int oldPos, final int newValue) {
        final int oldIndex = index[oldPos];
        shiftKeys(oldPos);
        final int newPos = ~find(newValue);
        value[newPos] = newValue;
        index[newPos] = oldIndex;
        indexToValue[oldIndex] = newValue;
    }

    private void shiftKeys(int pos) {
        // Shift entries with the same hash.
        int last, slot;
        int curr;
        final int[] value = this.value;
        final int[] index = this.index;
        while (true) {
            pos = ((last = pos) + 1) & mask;
            while (true) {
                if ((curr = value[pos]) == 0) {
                    value[last] = (0);
                    return;
                }
                slot = (HashCommon.mix(curr)) & mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
                pos = (pos + 1) & mask;
            }
            value[last] = curr;
            index[last] = index[pos];
        }
    }

    private void rehash(final int newN) {
        final int[] key = this.value;
        final int[] value = this.index;
        final int mask = newN - 1;
        final int[] newKey = new int[newN + 1];
        final int[] newValue = new int[newN + 1];
        int i = n, pos;
        for (int j = realSize(); j-- != 0;) {
            while (key[--i] == 0);
            if (!((newKey[pos = (HashCommon.mix((key[i]))) & mask]) == 0))
                while (!((newKey[pos = (pos + 1) & mask]) == 0));
            newKey[pos] = key[i];
            newValue[pos] = value[i];
        }
        newValue[newN] = value[n];
        n = newN;
        this.mask = mask;
        maxFill = n - (n >> 2);
        this.value = newKey;
        this.index = newValue;
    }

    private int realSize() {
        return containsNullKey ? size - 1 : size;
    }

    @Override
    public PaletteIndexMap clone() {
        PaletteIndexMap c;
        try {
            c = (PaletteIndexMap) super.clone();
        } catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.containsNullKey = containsNullKey;
        c.value = value.clone();
        c.index = index.clone();
        c.indexToValue = indexToValue.clone();
        return c;
    }
}
