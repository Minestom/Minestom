package net.minestom.server.instance.palette;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.IntPredicate;

/// Bidirectional, counted storage for an indirect palette.
///
/// Small palettes use a linear reverse lookup.
/// Larger palettes add an open addressed array whose buckets reference the forward value array, avoiding a second copy of every key.
/// Palette entries with a zero count remain available for reuse and therefore do not force growth.
final class PaletteTable implements Cloneable {
    static final int LINEAR_MAX_SIZE = 4;

    private int[] values;
    private int[] counts;
    private int @Nullable [] buckets;
    private int size;

    PaletteTable(int capacity) {
        if (capacity <= 0 || (capacity & capacity - 1) != 0) {
            throw new IllegalArgumentException("Capacity must be a positive power of two: " + capacity);
        }
        this.values = new int[capacity];
        this.counts = new int[capacity];
    }

    int capacity() {
        return values.length;
    }

    int size() {
        return size;
    }

    boolean linear() {
        return buckets == null;
    }

    int value(int index) {
        return values[index];
    }

    int countAt(int index) {
        return counts[index];
    }

    int[] values() {
        return values;
    }

    int indexOf(int value) {
        final int[] buckets = this.buckets;
        final int[] values = this.values;
        if (buckets == null) {
            for (int index = 0, size = this.size; index < size; index++) {
                if (values[index] == value) return index;
            }
            return -1;
        }
        final int mask = buckets.length - 1;
        int slot = homeSlot(value, mask);
        int encoded;
        while ((encoded = buckets[slot]) != 0) {
            final int index = encoded - 1;
            if (values[index] == value) return index;
            slot = slot + 1 & mask;
        }
        return -1;
    }

    /// Inserts an absent value with count zero, reusing a dead entry when the table is full.
    ///
    /// @return the palette index, or `-1` when every entry is live and the table must grow
    int insert(int value) {
        final int[] values = this.values;
        final int size = this.size;
        int[] buckets = this.buckets;
        if (buckets == null && size == LINEAR_MAX_SIZE && values.length > LINEAR_MAX_SIZE) {
            this.buckets = buckets = new int[values.length << 1];
            rebuildHash(buckets, values, size);
        }
        final int[] counts = this.counts;
        int index = size;
        if (index == values.length) {
            index = firstDeadIndex(counts, size);
            if (index == -1) return -1;
            if (buckets != null) removeHashIndex(buckets, values, index);
        } else {
            this.size = index + 1;
        }
        values[index] = value;
        counts[index] = 0;
        if (buckets != null) insertHashIndex(buckets, values, index);
        return index;
    }

    int insert(int value, int count) {
        final int index = insert(value);
        if (index == -1) return -1;
        if (count != 0) counts[index] = count;
        return index;
    }

    void grow(int newCapacity) {
        final int[] oldValues = this.values;
        if (newCapacity <= oldValues.length || (newCapacity & newCapacity - 1) != 0) {
            throw new IllegalArgumentException("Invalid grown capacity: " + newCapacity);
        }
        final int size = this.size;
        final int[] values = Arrays.copyOf(oldValues, newCapacity);
        final int[] counts = Arrays.copyOf(this.counts, newCapacity);
        final int[] buckets = size > LINEAR_MAX_SIZE ? new int[newCapacity << 1] : null;
        this.values = values;
        this.counts = counts;
        this.buckets = buckets;
        if (buckets != null) rebuildHash(buckets, values, size);
    }

    void replaceValue(int index, int value) {
        final int[] buckets = this.buckets;
        final int[] values = this.values;
        if (buckets != null) removeHashIndex(buckets, values, index);
        values[index] = value;
        if (buckets != null) insertHashIndex(buckets, values, index);
    }

    void offset(int offset) {
        final int[] values = this.values;
        final int size = this.size;
        for (int index = 0; index < size; index++) values[index] += offset;
        final int[] buckets = this.buckets;
        if (buckets != null) rebuildHash(buckets, values, size);
    }

    void moveOne(int oldIndex, int newIndex) {
        if (oldIndex == newIndex) return;
        final int[] counts = this.counts;
        final int oldCount = counts[oldIndex];
        assert oldCount > 0 : "Cannot decrement an unused palette entry";
        counts[oldIndex] = oldCount - 1;
        counts[newIndex]++;
    }

    void moveAll(int oldIndex, int newIndex) {
        if (oldIndex == newIndex) return;
        final int[] counts = this.counts;
        final int count = counts[oldIndex];
        if (count == 0) return;
        counts[oldIndex] = 0;
        counts[newIndex] += count;
    }

    void addCount(int index, int count) {
        if (count == 0) return;
        final int[] counts = this.counts;
        final int oldCount = counts[index];
        final int newCount = oldCount + count;
        if (newCount < 0) throw new IllegalStateException("Negative palette count");
        counts[index] = newCount;
    }

    void addCounts(int[] deltas) {
        final int[] counts = this.counts;
        for (int index = 0; index < deltas.length; index++) {
            final int delta = deltas[index];
            if (delta == 0) continue;
            final int count = counts[index] + delta;
            if (count < 0) throw new IllegalStateException("Negative palette count");
            counts[index] = count;
        }
    }

    int count(int value) {
        final int index = indexOf(value);
        return index == -1 ? 0 : counts[index];
    }

    int count(IntPredicate predicate) {
        final int[] counts = this.counts;
        final int[] values = this.values;
        int result = 0;
        for (int index = 0, size = this.size; index < size; index++) {
            final int count = counts[index];
            if (count != 0 && predicate.test(values[index])) result += count;
        }
        return result;
    }

    boolean any(IntPredicate predicate) {
        final int[] counts = this.counts;
        final int[] values = this.values;
        for (int index = 0, size = this.size; index < size; index++) {
            if (counts[index] != 0 && predicate.test(values[index])) return true;
        }
        return false;
    }

    boolean all(IntPredicate predicate) {
        final int[] counts = this.counts;
        final int[] values = this.values;
        for (int index = 0, size = this.size; index < size; index++) {
            if (counts[index] != 0 && !predicate.test(values[index])) return false;
        }
        return true;
    }

    void getAllCounts(Palette.ValueCountConsumer consumer) {
        final int[] counts = this.counts;
        final int[] values = this.values;
        for (int index = 0, size = this.size; index < size; index++) {
            final int count = counts[index];
            if (count != 0) consumer.accept(values[index], count);
        }
    }

    int singleLiveIndex() {
        final int[] counts = this.counts;
        int liveIndex = -1;
        for (int index = 0, size = this.size; index < size; index++) {
            if (counts[index] == 0) continue;
            if (liveIndex != -1) return -1;
            liveIndex = index;
        }
        return liveIndex;
    }

    private static int firstDeadIndex(int[] counts, int size) {
        for (int index = 0; index < size; index++) {
            if (counts[index] == 0) return index;
        }
        return -1;
    }

    /// Maps a value to its preferred bucket by Fibonacci hashing.
    ///
    /// The high bits of the golden ratio product are the well mixed ones, and they spread
    /// arithmetic runs of ids (like consecutive block states of one block) almost collision free.
    static int homeSlot(int value, int mask) {
        return (value * 0x9E3779B9) >>> Integer.numberOfLeadingZeros(mask);
    }

    private static void rebuildHash(int[] buckets, int[] values, int size) {
        Arrays.fill(buckets, 0);
        for (int index = 0; index < size; index++) insertHashIndex(buckets, values, index);
    }

    private static void insertHashIndex(int[] buckets, int[] values, int index) {
        final int mask = buckets.length - 1;
        int slot = homeSlot(values[index], mask);
        while (buckets[slot] != 0) slot = slot + 1 & mask;
        buckets[slot] = index + 1;
    }

    private static void removeHashIndex(int[] buckets, int[] values, int index) {
        final int mask = buckets.length - 1;
        int slot = homeSlot(values[index], mask);
        while (buckets[slot] != index + 1) {
            if (buckets[slot] == 0) throw new IllegalStateException("Missing palette hash entry");
            slot = slot + 1 & mask;
        }
        shiftHashEntries(buckets, values, slot);
    }

    private static void shiftHashEntries(int[] buckets, int[] values, int slot) {
        final int mask = buckets.length - 1;
        int last;
        while (true) {
            slot = (last = slot) + 1 & mask;
            int encoded;
            while ((encoded = buckets[slot]) != 0) {
                final int home = homeSlot(values[encoded - 1], mask);
                if (last <= slot ? last >= home || home > slot : last >= home && home > slot) break;
                slot = slot + 1 & mask;
            }
            if (encoded == 0) {
                buckets[last] = 0;
                return;
            }
            buckets[last] = encoded;
        }
    }

    @Override
    public PaletteTable clone() {
        final PaletteTable clone;
        try {
            clone = (PaletteTable) super.clone();
        } catch (CloneNotSupportedException exception) {
            throw new InternalError(exception);
        }
        clone.values = values.clone();
        clone.counts = counts.clone();
        final int[] buckets = this.buckets;
        if (buckets != null) clone.buckets = buckets.clone();
        return clone;
    }
}
