package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import static net.minestom.server.coordinate.CoordConversion.SECTION_BLOCK_COUNT;
import static net.minestom.server.instance.palette.Palettes.*;

final class PaletteImpl implements Palette {
    private static final ThreadLocal<int[]> WRITE_CACHE = ThreadLocal.withInitial(() -> new int[SECTION_BLOCK_COUNT]);
    final byte dimension, minBitsPerEntry, maxBitsPerEntry, directBits;

    byte bitsPerEntry = 0;
    int count = 0; // Serve as the single value if bitsPerEntry == 0

    long @UnknownNullability [] values; // null when bitsPerEntry == 0
    // null when using direct mode (bitsPerEntry > maxBitsPerEntry) or single mode (bitsPerEntry == 0)
    @UnknownNullability PaletteIndexMap paletteIndexMap;

    PaletteImpl(byte dimension, byte minBitsPerEntry, byte maxBitsPerEntry, byte directBits) {
        validateDimension(dimension);
        this.dimension = dimension;
        this.minBitsPerEntry = minBitsPerEntry;
        this.maxBitsPerEntry = maxBitsPerEntry;
        this.directBits = directBits;
    }

    PaletteImpl(byte dimension, byte minBitsPerEntry, byte maxBitsPerEntry, byte directBits, byte bitsPerEntry) {
        this(dimension, minBitsPerEntry, maxBitsPerEntry, directBits);

        this.bitsPerEntry = bitsPerEntry;
        if (bitsPerEntry != 0) {
            this.values = new long[arrayLength(dimension, bitsPerEntry)];

            if (!isDirect()) {
                this.paletteIndexMap = new PaletteIndexMap(bitsPerEntry);
                paletteIndexMap.valueToIndex(0);
            }
        }
    }

    @Override
    public int get(int x, int y, int z) {
        validateCoord(dimension, x, y, z);
        if (bitsPerEntry == 0) return count;
        final int value = read(dimension(), bitsPerEntry, values, x, y, z);
        return paletteIndexToValue(value);
    }

    @Override
    public void getAll(EntryConsumer consumer) {
        if (bitsPerEntry == 0) {
            Palettes.getAllFill(dimension, count, consumer);
        } else {
            retrieveAll(consumer, true);
        }
    }

    @Override
    public void getAllPresent(EntryConsumer consumer) {
        if (bitsPerEntry == 0) {
            if (count != 0) Palettes.getAllFill(dimension, count, consumer);
        } else {
            retrieveAll(consumer, false);
        }
    }

    @Override
    public int height(int x, int z, EntryPredicate predicate) {
        validateCoord(dimension, x, 0, z);
        final int dimension = this.dimension;
        final int startY = dimension - 1;
        if (bitsPerEntry == 0) return predicate.get(x, startY, z, count) ? startY : -1;
        final long[] values = this.values;
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = 64 / bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        final int[] paletteIds = isDirect() ? null : paletteIndexMap.indexToValueArray();
        for (int y = startY; y >= 0; y--) {
            final int index = sectionIndex(dimension, x, y, z);
            final int longIndex = index / valuesPerLong;
            final int bitIndex = (index % valuesPerLong) * bitsPerEntry;
            final int paletteIndex = (int) (values[longIndex] >> bitIndex) & mask;
            final int value = paletteIds != null ? paletteIds[paletteIndex] : paletteIndex;
            if (predicate.get(x, y, z, value)) return y;
        }
        return -1;
    }

    @Override
    public void set(int x, int y, int z, int value) {
        validateCoord(dimension, x, y, z);
        final int paletteIndex = valueToPaletteIndex(value);
        final int oldValue = Palettes.write(dimension(), bitsPerEntry, values, x, y, z, paletteIndex);
        // Check if block count needs to be updated
        final boolean currentAir = paletteIndexToValue(oldValue) == 0;
        if (currentAir != (value == 0)) this.count += currentAir ? 1 : -1;
    }

    @Override
    public void fill(int value) {
        this.bitsPerEntry = 0;
        this.count = value;
        this.values = null;
        this.paletteIndexMap = null;
    }

    @Override
    public void load(int[] palette, long[] values) {
        int bpe = palette.length <= 1 ? 0 : MathUtils.bitsToRepresent(palette.length - 1);
        bpe = Math.max(minBitsPerEntry, bpe);

        if (bpe > maxBitsPerEntry) {
            // Direct mode: convert from palette indices to direct values
            this.bitsPerEntry = directBits;
            this.paletteIndexMap = null;

            final AtomicInteger count = new AtomicInteger();
            this.values = Palettes.remap(dimension, bpe, directBits, values, true, v -> {
                final int result = palette[v];
                if (result != 0) count.setPlain(count.getPlain() + 1);
                return result;
            });
            this.count = count.getPlain();
        } else {
            // Indirect mode: use palette
            this.bitsPerEntry = (byte) bpe;
            this.paletteIndexMap = new PaletteIndexMap(palette);
            this.values = Arrays.copyOf(values, arrayLength(dimension, bitsPerEntry));
            recount();
        }
    }

    @Override
    public void offset(int offset) {
        if (offset == 0) return;
        if (bitsPerEntry == 0) {
            this.count += offset;
        } else {
            replaceAll((x, y, z, value) -> value + offset);
        }
    }

    @Override
    public void replace(int oldValue, int newValue) {
        if (oldValue == newValue) return;
        if (bitsPerEntry == 0) {
            if (oldValue == count) fill(newValue);
        } else {
            int oldIndex;
            int newIndex;
            if (isDirect()) {
                oldIndex = oldValue;
                newIndex = newValue;
            } else {
                final long replaceResult = paletteIndexMap.tryReplace(oldValue, newValue);
                if (replaceResult < 0) return; // Old value not present in palette
                oldIndex = (int) replaceResult;
                newIndex = (int) (replaceResult >> 32);
                if (oldIndex == newIndex) return;
            }
            final AtomicInteger count = new AtomicInteger();
            Palettes.remap(dimension, bitsPerEntry, bitsPerEntry, values, v -> {
               if (v == oldIndex) {
                   count.setPlain(count.getPlain() + 1);
                   return newIndex;
               }
               return v;
            });
            if (oldValue == 0) this.count += count.getPlain();
            if (newValue == 0) this.count -= count.getPlain();
        }
    }

    @Override
    public void setAll(EntrySupplier supplier) {
        int[] cache = WRITE_CACHE.get();
        final int dimension = dimension();
        // Fill cache with values
        int fillValue = -1;
        int count = 0;
        int index = 0;
        for (int y = 0; y < dimension; y++) {
            for (int z = 0; z < dimension; z++) {
                for (int x = 0; x < dimension; x++) {
                    int value = supplier.get(x, y, z);
                    // Support for fill fast exit if the supplier returns a constant value
                    if (fillValue != -2) {
                        if (fillValue == -1) {
                            fillValue = value;
                        } else if (fillValue != value) {
                            fillValue = -2;
                        }
                    }
                    // Set value in cache
                    if (value != 0) count++;
                    cache[index++] = value;
                }
            }
        }
        assert index == maxSize();
        // Update palette content
        if (fillValue < 0) {
            makeDirect();
            updateAll(cache);
            this.count = count;
        } else {
            fill(fillValue);
        }
    }

    @Override
    public void replace(int x, int y, int z, IntUnaryOperator operator) {
        validateCoord(dimension, x, y, z);
        final int oldValue = get(x, y, z);
        final int newValue = operator.applyAsInt(oldValue);
        if (oldValue != newValue) set(x, y, z, newValue);
    }

    @Override
    public void replaceAll(EntryFunction function) {
        int[] cache = WRITE_CACHE.get();
        AtomicInteger arrayIndex = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        getAll((x, y, z, value) -> {
            final int newValue = function.apply(x, y, z, value);
            final int index = arrayIndex.getPlain();
            arrayIndex.setPlain(index + 1);
            cache[index] = newValue;
            if (newValue != 0) count.setPlain(count.getPlain() + 1);
        });
        assert arrayIndex.getPlain() == maxSize();
        // Update palette content
        makeDirect();
        updateAll(cache);
        this.count = count.getPlain();
    }

    @Override
    public void copyFrom(Palette source, int offsetX, int offsetY, int offsetZ) {
        if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
            copyFrom(source);
            return;
        }

        final PaletteImpl sourcePalette = (PaletteImpl) source;
        final int sourceDimension = sourcePalette.dimension();
        final int targetDimension = this.dimension();
        if (sourceDimension != targetDimension) {
            throw new IllegalArgumentException("Source palette dimension (" + sourceDimension +
                    ") must equal target palette dimension (" + targetDimension + ")");
        }

        // Calculate the actual copy bounds - only copy what fits within target bounds
        final int maxX = Math.min(sourceDimension, targetDimension - offsetX);
        final int maxY = Math.min(sourceDimension, targetDimension - offsetY);
        final int maxZ = Math.min(sourceDimension, targetDimension - offsetZ);

        // Early exit if nothing to copy (offset pushes everything out of bounds)
        if (maxX <= 0 || maxY <= 0 || maxZ <= 0) {
            return;
        }

        // Fast path: if source is single-value palette
        if (sourcePalette.bitsPerEntry == 0) {
            // Fill the region with the single value - optimized loop order
            final int value = sourcePalette.count;
            final int paletteValue = valueToPaletteIndex(value);

            // Direct write to avoid repeated palette lookups
            for (int y = 0; y < maxY; y++) {
                final int targetY = offsetY + y;
                for (int z = 0; z < maxZ; z++) {
                    final int targetZ = offsetZ + z;
                    for (int x = 0; x < maxX; x++) {
                        final int targetX = offsetX + x;
                        final int oldValue = Palettes.write(targetDimension, bitsPerEntry, values, targetX, targetY, targetZ, paletteValue);
                        // Update count based on air transitions
                        final boolean wasAir = paletteIndexToValue(oldValue) == 0;
                        final boolean isAir = value == 0;
                        if (wasAir != isAir) {
                            this.count += wasAir ? 1 : -1;
                        }
                    }
                }
            }
            return;
        }

        // Source is empty, fill target region with air
        if (sourcePalette.count == 0) {
            if (this.count == 0) return;
            final int airPaletteIndex = valueToPaletteIndex(0);
            int removedBlocks = 0;
            for (int y = 0; y < maxY; y++) {
                final int targetY = offsetY + y;
                for (int z = 0; z < maxZ; z++) {
                    final int targetZ = offsetZ + z;
                    for (int x = 0; x < maxX; x++) {
                        final int targetX = offsetX + x;
                        final int oldValue = Palettes.write(targetDimension, bitsPerEntry, values, targetX, targetY, targetZ, airPaletteIndex);
                        if (paletteIndexToValue(oldValue) != 0) removedBlocks++;
                    }
                }
            }
            this.count -= removedBlocks;
            return;
        }

        // General case: copy each value individually with bounds checking
        // Use optimized access patterns to minimize cache misses
        final long[] sourceValues = sourcePalette.values;
        final int sourceBitsPerEntry = sourcePalette.bitsPerEntry;
        final int sourceMask = (1 << sourceBitsPerEntry) - 1;
        final int sourceValuesPerLong = 64 / sourceBitsPerEntry;
        final int sourceDimensionBitCount = MathUtils.bitsToRepresent(sourceDimension - 1);
        final int sourceShiftedDimensionBitCount = sourceDimensionBitCount << 1;
        final int[] sourcePaletteIds = sourcePalette.isDirect() ? null :
                sourcePalette.paletteIndexMap.indexToValueArray();

        int countDelta = 0;
        for (int y = 0; y < maxY; y++) {
            final int targetY = offsetY + y;
            for (int z = 0; z < maxZ; z++) {
                final int targetZ = offsetZ + z;
                for (int x = 0; x < maxX; x++) {
                    final int targetX = offsetX + x;

                    final int sourceIndex = y << sourceShiftedDimensionBitCount | z << sourceDimensionBitCount | x;
                    final int longIndex = sourceIndex / sourceValuesPerLong;
                    final int bitIndex = (sourceIndex - longIndex * sourceValuesPerLong) * sourceBitsPerEntry;
                    final int sourcePaletteIndex = (int) (sourceValues[longIndex] >> bitIndex) & sourceMask;
                    final int sourceValue = sourcePaletteIds != null && sourcePaletteIndex < sourcePaletteIds.length ?
                            sourcePaletteIds[sourcePaletteIndex] : sourcePaletteIndex;

                    // Convert to target palette index and write
                    final int targetPaletteIndex = valueToPaletteIndex(sourceValue);
                    final int oldValue = Palettes.write(targetDimension, bitsPerEntry, values, targetX, targetY, targetZ, targetPaletteIndex);

                    // Update count
                    final boolean wasAir = paletteIndexToValue(oldValue) == 0;
                    final boolean isAir = sourceValue == 0;
                    if (wasAir != isAir) {
                        countDelta += wasAir ? 1 : -1;
                    }
                }
            }
        }

        this.count += countDelta;
    }

    @Override
    public void copyFrom(Palette source) {
        final PaletteImpl sourcePalette = (PaletteImpl) source;
        final int sourceDimension = sourcePalette.dimension();
        final int targetDimension = this.dimension();
        if (sourceDimension != targetDimension) {
            throw new IllegalArgumentException("Source palette dimension (" + sourceDimension +
                    ") must equal target palette dimension (" + targetDimension + ")");
        }

        if (sourcePalette.bitsPerEntry == 0) {
            fill(sourcePalette.count);
            return;
        }
        if (sourcePalette.count == 0) {
            fill(0);
            return;
        }

        // Copy
        this.bitsPerEntry = sourcePalette.bitsPerEntry;
        this.count = sourcePalette.count;

        if (sourcePalette.values != null) {
            this.values = sourcePalette.values.clone();
        } else {
            this.values = null;
        }

        if (sourcePalette.paletteIndexMap != null) {
            this.paletteIndexMap = new PaletteIndexMap(
                    sourcePalette.paletteIndexMap.indexToValueArray(),
                    sourcePalette.paletteIndexMap.size());
        }
    }

    @Override
    public int count() {
        if (bitsPerEntry == 0) {
            return count == 0 ? 0 : maxSize();
        } else {
            return count;
        }
    }

    @Override
    public int count(int value) {
        if (bitsPerEntry == 0) return count == value ? maxSize() : 0;
        if (value == 0) return maxSize() - count();
        final int queryValue = valueToPalettIndexOrDefault(value);
        return countPaletteIndex(queryValue);
    }

    void recount() {
        if (bitsPerEntry != 0) {
            this.count = maxSize() - countPaletteIndex(valueToPalettIndexOrDefault(0));
        }
    }

    /// Assumes {@link PaletteImpl#bitsPerEntry} != 0
    int countPaletteIndex(int paletteIndex) {
        if (paletteIndex < 0) return 0;
        int result = 0;
        final int size = maxSize();
        final int bits = bitsPerEntry;
        final int valuesPerLong = 64 / bits;
        final int mask = (1 << bits) - 1;
        for (int i = 0, idx = 0; i < values.length; i++) {
            long block = values[i];
            int end = Math.min(valuesPerLong, size - idx);
            for (int j = 0; j < end; j++, idx++) {
                if (((int) (block & mask)) == paletteIndex) result++;
                block >>>= bits;
            }
        }
        return result;
    }

    @Override
    public boolean any(int value) {
        if (bitsPerEntry == 0) return count == value;
        if (value == 0) return maxSize() != count;
        int queryValue = valueToPalettIndexOrDefault(value);
        if (queryValue == -1) return false;
        // Scan through the values
        final int size = maxSize();
        final int bits = bitsPerEntry;
        final int valuesPerLong = 64 / bits;
        final int mask = (1 << bits) - 1;
        for (int i = 0, idx = 0; i < values.length; i++) {
            long block = values[i];
            int end = Math.min(valuesPerLong, size - idx);
            for (int j = 0; j < end; j++, idx++) {
                if (((int) (block & mask)) == queryValue) return true;
                block >>>= bits;
            }
        }
        return false;
    }

    @Override
    public int bitsPerEntry() {
        return bitsPerEntry;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public void optimize(Optimization focus) {
        final int bitsPerEntry = this.bitsPerEntry;
        if (bitsPerEntry == 0) {
            // Already optimized (single value)
            return;
        }

        // Count unique values
        IntSet uniqueValues = new IntOpenHashSet();
        getAll((x, y, z, value) -> uniqueValues.add(value));
        final int uniqueCount = uniqueValues.size();

        // If only one unique value, use fill for maximum optimization
        if (uniqueCount == 1) {
            fill(uniqueValues.iterator().nextInt());
            return;
        }

        if (focus == Optimization.SPEED) {
            // Speed optimization - use direct storage
            makeDirect();
        } else if (focus == Optimization.SIZE) {
            // Size optimization - calculate minimum bits needed for unique values
            final var paletteList = new IntArrayList(uniqueValues);
            downsizeWithPalette(paletteList);
        }
    }

    @Override
    public boolean compare(Palette p) {
        final PaletteImpl palette = (PaletteImpl) p;
        final int dimension = this.dimension();
        if (palette.dimension() != dimension) return false;
        if (palette.count != this.count) return false;
        if (palette.count == 0) return true;
        if (palette.bitsPerEntry == 0 && this.bitsPerEntry == 0) return true;
        for (int y = 0; y < dimension; y++) {
            for (int z = 0; z < dimension; z++) {
                for (int x = 0; x < dimension; x++) {
                    final int value1 = this.get(x, y, z);
                    final int value2 = palette.get(x, y, z);
                    if (value1 != value2) return false;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Palette clone() {
        PaletteImpl clone = new PaletteImpl(dimension, minBitsPerEntry, maxBitsPerEntry, directBits);
        clone.bitsPerEntry = this.bitsPerEntry;
        clone.count = this.count;
        if (bitsPerEntry == 0) return clone;
        clone.values = values.clone();
        if (paletteIndexMap != null) clone.paletteIndexMap = paletteIndexMap.clone();
        return clone;
    }

    private void retrieveAll(EntryConsumer consumer, boolean consumeEmpty) {
        if (!consumeEmpty && count == 0) return;
        final long[] values = this.values;
        final int dimension = this.dimension();
        final int bitsPerEntry = this.bitsPerEntry;
        final int magicMask = (1 << bitsPerEntry) - 1;
        final int valuesPerLong = 64 / bitsPerEntry;
        final int size = maxSize();
        final int dimensionMinus = dimension - 1;
        final int[] ids = isDirect() ? null : paletteIndexMap.indexToValueArray();
        final int dimensionBitCount = MathUtils.bitsToRepresent(dimensionMinus);
        final int shiftedDimensionBitCount = dimensionBitCount << 1;
        for (int i = 0; i < values.length; i++) {
            final long value = values[i];
            final int startIndex = i * valuesPerLong;
            final int endIndex = Math.min(startIndex + valuesPerLong, size);
            for (int index = startIndex; index < endIndex; index++) {
                final int bitIndex = (index - startIndex) * bitsPerEntry;
                final int paletteIndex = (int) (value >> bitIndex & magicMask);
                if (consumeEmpty || paletteIndex != 0) {
                    final int y = index >> shiftedDimensionBitCount;
                    final int z = index >> dimensionBitCount & dimensionMinus;
                    final int x = index & dimensionMinus;
                    final int result = ids != null && paletteIndex < ids.length ? ids[paletteIndex] : paletteIndex;
                    consumer.accept(x, y, z, result);
                }
            }
        }
    }

    private void updateAll(int[] paletteValues) {
        final int size = maxSize();
        assert paletteValues.length >= size;
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = 64 / bitsPerEntry;
        final long clear = (1L << bitsPerEntry) - 1L;
        final long[] values = this.values;
        for (int i = 0; i < values.length; i++) {
            long block = values[i];
            final int startIndex = i * valuesPerLong;
            final int endIndex = Math.min(startIndex + valuesPerLong, size);
            for (int index = startIndex; index < endIndex; index++) {
                final int bitIndex = (index - startIndex) * bitsPerEntry;
                block = block & ~(clear << bitIndex) | ((long) paletteValues[index] << bitIndex);
            }
            values[i] = block;
        }
    }

    /// Assumes {@link PaletteImpl#bitsPerEntry} != 0
    private void downsizeWithPalette(IntArrayList palette) {
        final byte bpe = this.bitsPerEntry;
        final byte newBpe = (byte) Math.max(MathUtils.bitsToRepresent(palette.size() - 1), minBitsPerEntry);
        if (newBpe >= bpe || newBpe > maxBitsPerEntry) return;

        final PaletteIndexMap newPaletteIndexMap = new PaletteIndexMap(palette.elements(), palette.size());

        if (isDirect()) {
            this.values = Palettes.remap(dimension, bpe, newBpe, values, newPaletteIndexMap::valueToIndexOrDefault);
        } else {
            final IntArrayList transformList = new IntArrayList(paletteIndexMap.size());
            final int[] indexToValueArray = paletteIndexMap.indexToValueArray();
            final int paletteIndexMapSize = paletteIndexMap.size();
            for (int index = 0; index < paletteIndexMapSize; index++) {
                transformList.add(indexToValueArray[index]);
            }
            final int[] transformArray = transformList.elements();
            this.values = Palettes.remap(dimension, bpe, newBpe, values, value -> transformArray[value]);
        }

        this.bitsPerEntry = newBpe;
        this.paletteIndexMap = newPaletteIndexMap;
    }

    void makeDirect() {
        if (isDirect()) return;
        if (bitsPerEntry == 0) {
            final int fillValue = this.count;
            this.values = new long[arrayLength(dimension, directBits)];
            if (fillValue != 0) {
                Palettes.fill(directBits, this.values, fillValue);
                this.count = maxSize();
            }
        } else {
            final int[] ids = paletteIndexMap.indexToValueArray();
            this.values = Palettes.remap(dimension, bitsPerEntry, directBits, values, v -> ids[v]);
        }
        this.paletteIndexMap = null;
        this.bitsPerEntry = directBits;
    }

    /// Assumes {@link PaletteImpl#bitsPerEntry} != 0
    void upsize() {
        final byte bpe = this.bitsPerEntry;
        byte newBpe = (byte) (bpe + 1);
        if (newBpe > maxBitsPerEntry) {
            makeDirect();
        } else {
            this.values = Palettes.remap(dimension, bpe, newBpe, values, (v) -> v);
            this.bitsPerEntry = newBpe;
        }
    }

    /// Assumes {@link PaletteImpl#bitsPerEntry} == 0
    void initIndirect() {
        final int fillValue = this.count;
        this.paletteIndexMap = new PaletteIndexMap(minBitsPerEntry);
        paletteIndexMap.valueToIndex(fillValue);
        this.bitsPerEntry = minBitsPerEntry;
        this.values = new long[arrayLength(dimension, minBitsPerEntry)];
        this.count = fillValue == 0 ? 0 : maxSize();
    }

    /// Assumes {@link PaletteImpl#bitsPerEntry} != 0
    @Override
    public int paletteIndexToValue(int paletteIndex) {
        return isDirect() ? paletteIndex : paletteIndexMap.indexToValue(paletteIndex);
    }

    @Override
    public int valueToPaletteIndex(int value) {
        if (isDirect()) return value;
        if (values == null) initIndirect();

        final int pos = paletteIndexMap.find(value);
        if (pos >= 0) return paletteIndexMap.UNSAFE_getIndex(pos);
        if (paletteIndexMap.size() >= maxPaletteSize(bitsPerEntry)) {
            // Palette is full, must resize
            upsize();
            if (isDirect()) return value;
        }
        return paletteIndexMap.UNSAFE_insert(~pos, value);
    }

    /// Assumes {@link PaletteImpl#bitsPerEntry} != 0
    int valueToPalettIndexOrDefault(int value) {
        return isDirect() ? value : paletteIndexMap.valueToIndexOrDefault(value);
    }

    @Override
    public int singleValue() {
        return bitsPerEntry == 0 ? count : -1;
    }

    @Override
    public long @Nullable [] indexedValues() {
        return values;
    }

//    boolean hasPalette() {
//        return bitsPerEntry <= maxBitsPerEntry;
//    }

    boolean isDirect() {
        return bitsPerEntry > maxBitsPerEntry;
    }

    private static void validateCoord(int dimension, int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0)
            throw new IllegalArgumentException("Coordinates must be non-negative");
        if (x >= dimension || y >= dimension || z >= dimension)
            throw new IllegalArgumentException("Coordinates must be less than the dimension size, got " + x + ", " + y + ", " + z + " for dimension " + dimension);
    }

    private static void validateDimension(int dimension) {
        if (dimension <= 1 || (dimension & dimension - 1) != 0)
            throw new IllegalArgumentException("Dimension must be a positive power of 2, got " + dimension);
    }
}
