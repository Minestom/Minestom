package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
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
    final byte dimension, minBitsPerEntry, maxBitsPerEntry;
    byte directBits;

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
    public void set(int x, int y, int z, int value) {
        validateCoord(dimension, x, y, z);
        final int paletteIndex = valueToPaletteIndex(value);
        final int oldValue = Palettes.write(dimension(), bitsPerEntry, values, x, y, z, paletteIndex);
        // Check if block count needs to be updated
        final boolean currentAir = paletteIndexToValue(oldValue) == 0;
        if (currentAir != (value == 0)) this.count += currentAir ? 1 : -1;
    }

    @Override
    public void setAll(EntrySupplier supplier) {
        final int[] cache = WRITE_CACHE.get();
        final int dimension = dimension();
        final int maxPaletteSize = 1 << maxBitsPerEntry;
        // Fill cache with values
        @Nullable
        PaletteIndexMap newPaletteIndexMap = new PaletteIndexMap(minBitsPerEntry);
        int count = 0;
        int index = 0;
        for (int y = 0; y < dimension; y++) {
            for (int z = 0; z < dimension; z++) {
                for (int x = 0; x < dimension; x++) {
                    final int value = supplier.get(x, y, z);
                    if (value != 0) count++;
                    if (newPaletteIndexMap == null) {
                        checkValue(value, false);
                        cache[index++] = value;
                        continue;
                    }
                    final int maybePaletteIndex = newPaletteIndexMap.valueToIndexCapped(value, maxPaletteSize);
                    if (maybePaletteIndex < 0) {
                        for (int i = 0; i < index; i++) cache[i] = newPaletteIndexMap.indexToValue(cache[i]);
                        checkValue(newPaletteIndexMap.maxValue(), false);
                        newPaletteIndexMap = null;
                        cache[index++] = value;
                        continue;
                    }
                    cache[index++] = maybePaletteIndex;
                }
            }
        }
        this.count = count;
        updateAll(cache, newPaletteIndexMap);
    }

    @Override
    public void replace(int oldValue, int newValue) {
        if (oldValue == newValue) return;
        if (bitsPerEntry == 0) {
            if (oldValue == count) fill(newValue);
        } else {
            final int oldIndex;
            final int newIndex;
            if (isDirect()) {
                checkValue(newValue, true);
                oldIndex = oldValue;
                newIndex = newValue;
            } else {
                final int oldPos = paletteIndexMap.find(oldValue);
                if (oldPos < 0) return;
                oldIndex = paletteIndexMap.UNSAFE_getIndex(oldPos);
                final int newPos = paletteIndexMap.find(newValue);
                if (newPos < 0) {
                    if (oldValue == 0 || newValue == 0) {
                        final int count = Palettes.count(dimension, bitsPerEntry, values, oldIndex);
                        if (oldValue == 0) this.count += count;
                        if (newValue == 0) this.count -= count;
                    }
                    paletteIndexMap.UNSAFE_replace(oldPos, newValue);
                    return;
                }
                newIndex = paletteIndexMap.UNSAFE_getIndex(newPos);
            }
            final AtomicInteger count = new AtomicInteger();
            this.values = Palettes.remap(dimension, bitsPerEntry, bitsPerEntry, values, v -> {
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
    public void replace(int x, int y, int z, IntUnaryOperator operator) {
        validateCoord(dimension, x, y, z);
        final int oldValue = get(x, y, z);
        final int newValue = operator.applyAsInt(oldValue);
        if (oldValue != newValue) set(x, y, z, newValue);
    }

    @Override
    public void replaceAll(EntryFunction function) {
        final int[] cache = WRITE_CACHE.get();
        final int maxPaletteSize = 1 << maxBitsPerEntry;
        final var consumer = new EntryConsumer() {
            @Nullable
            PaletteIndexMap newPaletteIndexMap = new PaletteIndexMap(minBitsPerEntry);
            int index = 0;
            int count = 0;

            @Override
            public void accept(int x, int y, int z, int oldValue) {
                final int value = function.apply(x, y, z, oldValue);
                if (value != 0) count++;
                if (newPaletteIndexMap == null) {
                    checkValue(value, false);
                    cache[index++] = value;
                    return;
                }
                final int maybePaletteIndex = newPaletteIndexMap.valueToIndexCapped(value, maxPaletteSize);
                if (maybePaletteIndex < 0) {
                    for (int i = 0; i < index; i++) cache[i] = newPaletteIndexMap.indexToValue(cache[i]);
                    checkValue(newPaletteIndexMap.maxValue(), false);
                    newPaletteIndexMap = null;
                    cache[index++] = value;
                    return;
                }
                cache[index++] = maybePaletteIndex;
            }
        };
        getAll(consumer);
        // Update palette content
        this.count = consumer.count;
        updateAll(cache, consumer.newPaletteIndexMap);
    }

    @Override
    public void fill(int value) {
        this.bitsPerEntry = 0;
        this.count = value;
        this.values = null;
        this.paletteIndexMap = null;
    }

    @Override
    public void fill(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int value) {
        validateCoord(dimension, minX, minY, minZ);
        validateCoord(dimension, maxX, maxY, maxZ);
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("Partial fill coordinates must be ordered from low to high");
        }

        final int dimensionMinus = dimension - 1;
        if (minX == 0 && minY == 0 && minZ == 0 &&
                maxX == dimensionMinus && maxY == dimensionMinus && maxZ == dimensionMinus) {
            fill(value);
            return;
        }

        final int paletteIndex = valueToPaletteIndex(value);
        final int airPaletteIndex = valueToPalettIndexOrDefault(0);

        this.count += Palettes.fill(
                minX, minY, minZ,
                maxX, maxY, maxZ,
                paletteIndex, airPaletteIndex,
                values, dimension, bitsPerEntry);
    }

    @Override
    public void offset(int offset) {
        if (offset == 0) return;
        if (bitsPerEntry == 0) {
            this.count += offset;
        } else {
            replaceAll((_, _, _, value) -> value + offset);
        }
    }

    @Override
    public void copyFrom(Palette source, int offsetX, int offsetY, int offsetZ) {
        if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
            copyFrom(source);
            return;
        }

        final PaletteImpl sourcePalette = (PaletteImpl) source;
        final int dimension = this.dimension;
        if (sourcePalette.dimension != dimension) {
            throw new IllegalArgumentException("Source palette dimension (" + sourcePalette.dimension +
                    ") must equal target palette dimension (" + dimension + ")");
        }


        // Calculate the actual copy bounds - only copy what fits within target bounds
        final int dimensionMinus = dimension - 1;
        final int minX = Math.max(0, offsetX);
        final int minY = Math.max(0, offsetY);
        final int minZ = Math.max(0, offsetZ);
        final int maxX = dimensionMinus - Math.max(0, -offsetX);
        final int maxY = dimensionMinus - Math.max(0, -offsetY);
        final int maxZ = dimensionMinus - Math.max(0, -offsetZ);

        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("Copy offset must not exceed palette bounds");
        }

        // Fast path: if source is single-value palette
        if (sourcePalette.bitsPerEntry == 0 || sourcePalette.count == 0) {
            fill(minX, minY, minZ, maxX, maxY, maxZ, sourcePalette.count);
            return;
        }

        // General case: copy each value individually with bounds checking
        // Use optimized access patterns to minimize cache misses
        final int dimensionBitCount = MathUtils.bitsToRepresent(dimension - 1);
        final int shiftedDimensionBitCount = dimensionBitCount << 1;

        final long[] sourceValues = sourcePalette.values;
        final int sourceBitsPerEntry = sourcePalette.bitsPerEntry;
        final int sourceMask = (1 << sourceBitsPerEntry) - 1;
        final int sourceValuesPerLong = 64 / sourceBitsPerEntry;
        final int[] sourcePaletteIds = sourcePalette.isDirect() ? null :
                sourcePalette.paletteIndexMap.indexToValueArray();

        int countDelta = 0;
        for (int y = minY; y <= maxY; y++) {
            final int sourceY = y - offsetY;
            for (int z = minZ; z <= maxZ; z++) {
                final int sourceZ = z - offsetZ;
                for (int x = minX; x <= maxX; x++) {
                    final int sourceX = x - offsetX;

                    final int sourceIndex = sourceY << shiftedDimensionBitCount |
                            sourceZ << dimensionBitCount | sourceX;
                    final int longIndex = sourceIndex / sourceValuesPerLong;
                    final int bitIndex = (sourceIndex - longIndex * sourceValuesPerLong) * sourceBitsPerEntry;
                    final int sourcePaletteIndex = (int) (sourceValues[longIndex] >> bitIndex) & sourceMask;
                    final int sourceValue = sourcePaletteIds != null ?
                            sourcePaletteIds[sourcePaletteIndex] : sourcePaletteIndex;

                    // Convert to target palette index and write
                    final int targetPaletteIndex = valueToPaletteIndex(sourceValue);
                    final int oldValue = Palettes.write(dimension, bitsPerEntry, values, x, y, z, targetPaletteIndex);

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
        this.directBits = sourcePalette.directBits;

        if (sourcePalette.values != null) {
            this.values = sourcePalette.values.clone();
        } else {
            this.values = null;
        }

        if (sourcePalette.paletteIndexMap != null) {
            this.paletteIndexMap = sourcePalette.paletteIndexMap.clone();
        } else {
            this.paletteIndexMap = null;
        }
    }

    @Override
    public void load(int[] palette, long[] values) {
        int bpe = palette.length <= 1 ? 0 : MathUtils.bitsToRepresent(palette.length - 1);
        bpe = Math.max(minBitsPerEntry, bpe);

        if (bpe > maxBitsPerEntry) {
            // Direct mode: convert from palette indices to direct values
            for (final int value : palette) checkValue(value, false);

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

        return Palettes.count(dimension, bitsPerEntry, values, queryValue);
    }

    void recount() {
        if (bitsPerEntry != 0) {
            final int zeroCount = Palettes.count(dimension, bitsPerEntry, values, valueToPalettIndexOrDefault(0));
            this.count = maxSize() - zeroCount;
        }
    }

    @Override
    public boolean any(int value) {
        if (bitsPerEntry == 0) return count == value;
        if (value == 0) return maxSize() != count;
        final int queryValue = valueToPalettIndexOrDefault(value);
        if (queryValue == -1) return false;
        // Scan through the values
        final int size = maxSize();
        final int bits = bitsPerEntry;
        final int valuesPerLong = 64 / bits;
        final int mask = (1 << bits) - 1;
        for (int i = 0, idx = 0; i < values.length; i++) {
            long block = values[i];
            final int end = Math.min(valuesPerLong, size - idx);
            for (int j = 0; j < end; j++, idx++) {
                if (((int) (block & mask)) == queryValue) return true;
                block >>>= bits;
            }
        }
        return false;
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
    public int bitsPerEntry() {
        return bitsPerEntry;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public void optimize(Optimization focus) {
        if (bitsPerEntry == 0) return;

        if (focus == Optimization.SPEED) {
            final PaletteIndexMap newPalette = collectOptimizedPalette((byte) 0);
            if (newPalette == null) {
                makeDirect();
                return;
            }
            fill(newPalette.indexToValue(0));
        } else if (focus == Optimization.SIZE) {
            final PaletteIndexMap newPalette = collectOptimizedPalette((byte) Math.min(bitsPerEntry, maxBitsPerEntry));
            if (newPalette == null) return;
            downsizeWithPalette(newPalette);
        }
    }

    /// Assumes bitsPerEntry != 0
    private @Nullable PaletteIndexMap collectOptimizedPalette(byte maxBitsPerEntry) {
        final int size = maxSize();
        final int bits = bitsPerEntry;
        final int valuesPerLong = 64 / bits;
        final int mask = (1 << bits) - 1;

        final PaletteIndexMap result = new PaletteIndexMap((byte) Math.max(1, maxBitsPerEntry));
        final int maxPaletteSize = 1 << maxBitsPerEntry;
        for (int i = 0, idx = 0; i < values.length; i++) {
            long block = values[i];
            final int end = Math.min(valuesPerLong, size - idx);
            for (int j = 0; j < end; j++, idx++) {
                final int paletteIndex = (int) (block & mask);
                final int value = paletteIndexToValue(paletteIndex);
                final int pos = result.find(value);
                if (pos < 0) {
                    if (result.size() >= maxPaletteSize) return null;
                    result.UNSAFE_insert(~pos, value);
                }
                block >>>= bits;
            }
        }
        return result;
    }

    @Override
    public boolean compare(Palette p) {
        if (this == p) return true;
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
        final PaletteImpl clone = new PaletteImpl(dimension, minBitsPerEntry, maxBitsPerEntry, directBits);
        clone.bitsPerEntry = this.bitsPerEntry;
        clone.count = this.count;
        if (bitsPerEntry == 0) return clone;
        clone.values = values.clone();
        if (paletteIndexMap != null) clone.paletteIndexMap = paletteIndexMap.clone();
        return clone;
    }

    private void retrieveAll(EntryConsumer consumer, boolean consumeEmpty) {
        if (!consumeEmpty && count == 0) return;
        final int size = maxSize();
        final int bits = bitsPerEntry;
        final int valuesPerLong = 64 / bits;
        final int mask = (1 << bits) - 1;
        final int dimensionMinus = dimension - 1;
        final int dimensionBits = MathUtils.bitsToRepresent(dimensionMinus);
        final int dimensionBitsShifted = dimensionBits << 1;

        for (int i = 0, idx = 0; i < values.length; i++) {
            long block = values[i];
            final int end = Math.min(valuesPerLong, size - idx);
            for (int j = 0; j < end; j++, idx++) {
                final int paletteIndex = (int) (block & mask);
                final int value = paletteIndexToValue(paletteIndex);
                if (consumeEmpty || value != 0) {
                    final int x = idx & dimensionMinus;
                    final int y = (idx >> dimensionBitsShifted) & dimensionMinus;
                    final int z = (idx >> dimensionBits) & dimensionMinus;
                    consumer.accept(x, y, z, value);
                }
                block >>>= bits;
            }
        }
    }

    private void updateAll(int[] paletteValues, @Nullable PaletteIndexMap newPaletteIndexMap) {
        final int size = maxSize();
        assert paletteValues.length >= size;
        final byte bpe;
        if (newPaletteIndexMap == null) {
            bpe = directBits;
        } else {
            if (newPaletteIndexMap.size() == 1) {
                fill(newPaletteIndexMap.indexToValue(0));
                return;
            }
            bpe = (byte) MathUtils.bitsToRepresent(Math.max(minBitsPerEntry, newPaletteIndexMap.size() - 1));
        }

        final int valuesPerLong = 64 / bpe;
        final long[] values = bpe == bitsPerEntry ? this.values : new long[arrayLength(dimension, bpe)];
        for (int i = 0, idx = 0; i < values.length; i++) {
            long block = 0;
            final int end = Math.min(valuesPerLong, size - idx) * bpe;
            for (int j = 0; j < end; j += bpe, idx++) {
                block |= (long) paletteValues[idx] << j;
            }
            values[i] = block;
        }
        this.values = values;
        this.bitsPerEntry = bpe;
        this.paletteIndexMap = newPaletteIndexMap;
    }

    private void downsizeWithPalette(PaletteIndexMap palette) {
        if (palette.size() == 1) {
            fill(palette.indexToValue(0));
            return;
        }
        final byte bpe = this.bitsPerEntry;
        final byte newBpe = (byte) Math.max(MathUtils.bitsToRepresent(palette.size() - 1), minBitsPerEntry);
        if (newBpe >= bpe || newBpe > maxBitsPerEntry) return;

        if (isDirect()) {
            this.values = Palettes.remap(dimension, bpe, newBpe, values, palette::valueToIndexOrDefault);
        } else {
            final int[] indexToValueArray = paletteIndexMap.indexToValueArray();
            final int paletteIndexMapSize = paletteIndexMap.size();
            final int[] transform = new int[paletteIndexMapSize];
            for (int index = 0; index < paletteIndexMapSize; index++) {
                transform[index] = palette.valueToIndexOrDefault(indexToValueArray[index]);
            }
            this.values = Palettes.remap(dimension, bpe, newBpe, values, value -> transform[value]);
        }

        this.bitsPerEntry = newBpe;
        this.paletteIndexMap = palette;
    }

    void makeDirect() {
        if (isDirect()) return;
        if (bitsPerEntry == 0) {
            final int fillValue = this.count;
            checkValue(fillValue, false);
            this.values = new long[arrayLength(dimension, directBits)];
            if (fillValue != 0) {
                Palettes.fill(directBits, this.values, fillValue);
                this.count = maxSize();
            }
        } else {
            checkValue(paletteIndexMap.maxValue(), false);
            final int[] ids = paletteIndexMap.indexToValueArray();
            this.values = Palettes.remap(dimension, bitsPerEntry, directBits, values, v -> ids[v]);
        }
        this.paletteIndexMap = null;
        this.bitsPerEntry = directBits;
    }

    /// Assumes {@link PaletteImpl#bitsPerEntry} != 0
    void upsize() {
        final byte bpe = this.bitsPerEntry;
        final byte newBpe = (byte) (bpe + 1);
        if (newBpe > maxBitsPerEntry) {
            makeDirect();
        } else {
            this.values = Palettes.remap(dimension, bpe, newBpe, values, Int2IntFunction.identity());
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
        if (isDirect()) {
            checkValue(value, true);
            return value;
        }
        if (values == null) initIndirect();

        final int pos = paletteIndexMap.find(value);
        if (pos >= 0) return paletteIndexMap.UNSAFE_getIndex(pos);
        if (paletteIndexMap.size() >= (1 << bitsPerEntry)) {
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

    void checkValue(int value, boolean allowResize) {
        if (value < 1 << directBits) return;
        final byte newDirectBits = (byte) MathUtils.bitsToRepresent(value);
        if (allowResize && isDirect()) {
            this.values = Palettes.remap(dimension, directBits, newDirectBits, values, Int2IntFunction.identity());
            this.bitsPerEntry = newDirectBits;
        }
        this.directBits = newDirectBits;
    }

    @Override
    public int singleValue() {
        return bitsPerEntry == 0 ? count : -1;
    }

    @Override
    public long @Nullable [] indexedValues() {
        return values;
    }

    boolean isDirect() {
        return bitsPerEntry > maxBitsPerEntry;
    }
}
