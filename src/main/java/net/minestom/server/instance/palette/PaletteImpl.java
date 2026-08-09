package net.minestom.server.instance.palette;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import static net.minestom.server.instance.palette.Palettes.arrayLength;

final class PaletteImpl implements Palette {
    final byte dimension, minBitsPerEntry, maxBitsPerEntry, directBits;

    byte bitsPerEntry;
    int singleValue;

    long @Nullable [] values; // Nullable, but we never do null checks as bpe controls
    @Nullable PaletteTable table;

    PaletteImpl(byte dimension, byte minBitsPerEntry, byte maxBitsPerEntry, byte directBits) {
        validateDimension(dimension);
        validateConfiguration(minBitsPerEntry, maxBitsPerEntry, directBits);
        this.dimension = dimension;
        this.minBitsPerEntry = minBitsPerEntry;
        this.maxBitsPerEntry = maxBitsPerEntry;
        this.directBits = directBits;
    }

    PaletteImpl(byte dimension, byte minBitsPerEntry, byte maxBitsPerEntry, byte directBits, byte bitsPerEntry) {
        this(dimension, minBitsPerEntry, maxBitsPerEntry, directBits);
        validateBitsPerEntry(minBitsPerEntry, maxBitsPerEntry, directBits, bitsPerEntry);
        this.bitsPerEntry = bitsPerEntry;
        if (isSingle(bitsPerEntry)) return;
        this.values = new long[arrayLength(dimension, bitsPerEntry)];
        if (bitsPerEntry <= maxBitsPerEntry) {
            final PaletteTable table = new PaletteTable(Palettes.maxPaletteSize(bitsPerEntry));
            table.insert(0, Palettes.maxSize(dimension));
            this.table = table;
        }
    }

    @Override
    public int get(int x, int y, int z) {
        final byte dimension = this.dimension;
        validateCoord(dimension, x, y, z);
        final int bitsPerEntry = this.bitsPerEntry;
        if (isSingle(bitsPerEntry)) return singleValue;
        final PaletteTable table = this.table;
        return Palettes.readValue(dimension, bitsPerEntry, values,
                paletteValues(table), x, y, z);
    }

    private static int getUnchecked(PaletteImpl palette, int dimension, int x, int y, int z) {
        final int bitsPerEntry = palette.bitsPerEntry;
        if (isSingle(bitsPerEntry)) return palette.singleValue;
        final PaletteTable table = palette.table;
        return Palettes.readValue(dimension, bitsPerEntry, palette.values,
                paletteValues(table), x, y, z);
    }

    @Override
    public void getAll(EntryConsumer consumer) {
        final int bitsPerEntry = this.bitsPerEntry;
        final byte dimension = this.dimension;
        if (isSingle(bitsPerEntry)) {
            Palettes.getAllFill(dimension, singleValue, consumer);
            return;
        }
        final PaletteTable table = this.table;
        if (isIndirect(table)) Palettes.getAllIndirect(dimension, bitsPerEntry, values, table.values(), consumer);
        else Palettes.getAllDirect(dimension, bitsPerEntry, values, consumer);
    }

    @Override
    public int height(int x, int z, EntryPredicate predicate) {
        final int dimension = this.dimension;
        validateCoord(dimension, x, 0, z);
        final int startY = dimension - 1;
        final int bitsPerEntry = this.bitsPerEntry;
        if (isSingle(bitsPerEntry)) return predicate.get(x, startY, z, singleValue) ? startY : -1;
        final PaletteTable table = this.table;
        return Palettes.height(dimension, bitsPerEntry, values,
                paletteValues(table), x, z, predicate);
    }

    @Override
    public void set(int x, int y, int z, int value) {
        final int dimension = this.dimension;
        validateCoord(dimension, x, y, z);
        validateValue(value);
        setUnchecked(this, dimension, x, y, z, value);
    }

    private static void setUnchecked(PaletteImpl palette, int dimension, int x, int y, int z, int value) {
        int bitsPerEntry = palette.bitsPerEntry;
        long[] values = palette.values;
        PaletteTable table = palette.table;
        if (isSingle(bitsPerEntry)) {
            if (palette.singleValue == value) return;
            bitsPerEntry = palette.minBitsPerEntry;
            values = new long[arrayLength(dimension, bitsPerEntry)];
            table = initIndirect(palette, dimension, bitsPerEntry, values);
        }
        if (!isIndirect(table)) {
            Palettes.writeValue(dimension, bitsPerEntry, values, x, y, z, value);
            return;
        }
        final int oldIndex = Palettes.read(dimension, bitsPerEntry, values, x, y, z);
        if (table.value(oldIndex) == value) return;
        final int paletteIndex = table.indexOf(value);
        if (paletteIndex == -1) {
            setNewPaletteValue(palette, dimension, bitsPerEntry, values, table,
                    x, y, z, oldIndex, value);
            return;
        }
        Palettes.writeValue(dimension, bitsPerEntry, values, x, y, z, paletteIndex);
        table.moveOne(oldIndex, paletteIndex);
    }

    private static void setNewPaletteValue(PaletteImpl palette, int dimension, int bitsPerEntry,
                                           long[] values, PaletteTable table,
                                           int x, int y, int z, int oldIndex, int value) {
        if (table.countAt(oldIndex) == 1) {
            table.replaceValue(oldIndex, value);
            return;
        }
        int newIndex = table.insert(value);
        if (newIndex == -1) {
            table = upsize(palette, dimension, bitsPerEntry, values, table);
            if (!isIndirect(table)) {
                Palettes.writeValue(dimension, palette.bitsPerEntry, palette.values, x, y, z, value);
                return;
            }
            newIndex = table.insert(value);
            assert newIndex != -1 : "Grown palette has no free index";
            bitsPerEntry = palette.bitsPerEntry;
            values = palette.values;
        }
        Palettes.writeValue(dimension, bitsPerEntry, values, x, y, z, newIndex);
        table.moveOne(oldIndex, newIndex);
    }

    @Override
    public void fill(int value) {
        validateValue(value);
        this.bitsPerEntry = 0;
        this.singleValue = value;
        this.values = null;
        this.table = null;
    }

    @Override
    public void load(int[] palette, long[] values) {
        if (palette.length == 0) throw new IllegalArgumentException("Palette cannot be empty");
        validateValues(palette);
        final int dimension = this.dimension;
        final int maxBitsPerEntry = this.maxBitsPerEntry;
        final byte directBits = this.directBits;
        final int sourceBits = Math.max(this.minBitsPerEntry, Palettes.bitsToIndex(palette.length));
        if (sourceBits > maxBitsPerEntry && directBits <= maxBitsPerEntry) {
            // More entries than the value space holds, rebuild to keep indirect storage
            final int[] lookup = Arrays.copyOf(palette, Palettes.maxPaletteSize(sourceBits));
            store(this, Palettes.unpackValues(Palettes.maxSize(dimension), sourceBits, values, lookup));
        } else if (sourceBits > maxBitsPerEntry) {
            this.bitsPerEntry = directBits;
            this.table = null;
            this.values = Palettes.remap(dimension, sourceBits, directBits, values,
                    index -> index < palette.length ? palette[index] : 0);
        } else {
            loadIndirect((byte) sourceBits, palette, values);
        }
    }

    void loadIndirect(byte bitsPerEntry, int[] palette, long[] values) {
        final int dimension = this.dimension;
        final PaletteTable table = new PaletteTable(Palettes.maxPaletteSize(bitsPerEntry));
        int[] canonicalIndices = null;
        for (int index = 0; index < palette.length; index++) {
            final int value = palette[index];
            int canonicalIndex = table.indexOf(value);
            if (canonicalIndex == -1) canonicalIndex = table.insert(value);
            else if (canonicalIndices == null) {
                canonicalIndices = new int[palette.length];
                for (int previous = 0; previous < index; previous++) canonicalIndices[previous] = previous;
            }
            if (canonicalIndices != null) canonicalIndices[index] = canonicalIndex;
        }

        final long[] newValues;
        final int mask = (1 << bitsPerEntry) - 1;
        // Sized from the mask so that indexing by `& mask` is provably in bounds
        final int[] deltas = new int[mask + 1];
        if (canonicalIndices == null) {
            newValues = Arrays.copyOf(values, arrayLength(dimension, bitsPerEntry));
            final int valuesPerLong = 64 / bitsPerEntry;
            final int size = Palettes.maxSize(dimension);
            final int fullLongs = size / valuesPerLong;
            // Two tallies so neighbouring lanes do not queue on the same counter, merged at the end.
            // Widths of 3 and 7 bits leave an odd lane over, which is counted on its own.
            final int pairedLanes = valuesPerLong & ~1;
            final int[] deltasB = new int[mask + 1];
            for (int i = 0; i < fullLongs; i++) {
                final long packed = newValues[i];
                for (int lane = 0; lane < pairedLanes; lane += 2) {
                    deltas[(int) (packed >>> (lane * bitsPerEntry)) & mask]++;
                    deltasB[(int) (packed >>> ((lane + 1) * bitsPerEntry)) & mask]++;
                }
                if (pairedLanes != valuesPerLong) {
                    deltas[(int) (packed >>> (pairedLanes * bitsPerEntry)) & mask]++;
                }
            }
            for (int index = fullLongs * valuesPerLong; index < size; index++) {
                final long packed = newValues[index / valuesPerLong];
                deltas[(int) (packed >>> ((index % valuesPerLong) * bitsPerEntry)) & mask]++;
            }
            for (int i = 0; i <= mask; i++) deltas[i] += deltasB[i];
        } else {
            final int[] remapping = canonicalIndices;
            newValues = Palettes.remap(dimension, bitsPerEntry, bitsPerEntry, values, index -> {
                final int canonicalIndex = remapping[index];
                deltas[canonicalIndex]++;
                return canonicalIndex;
            });
        }
        table.addCounts(deltas);
        this.bitsPerEntry = bitsPerEntry;
        this.values = newValues;
        this.table = table;
    }

    @Override
    public void offset(int offset) {
        if (offset == 0) return;
        if (isSingle(bitsPerEntry)) {
            final int newValue = singleValue + offset;
            validateValue(newValue);
            this.singleValue = newValue;
            return;
        }
        final PaletteTable table = this.table;
        if (isIndirect(table)) {
            validateTableValues(table, offset);
            table.offset(offset);
            return;
        }
        replaceAll((_, _, _, value) -> value + offset);
    }

    @Override
    public void replace(int oldValue, int newValue) {
        validateValue(newValue);
        if (oldValue == newValue) return;
        final int bitsPerEntry = this.bitsPerEntry;
        if (isSingle(bitsPerEntry)) {
            if (oldValue == singleValue) fill(newValue);
            return;
        }
        final long[] values = this.values;
        final PaletteTable table = this.table;
        final int dimension = this.dimension;
        if (!isIndirect(table)) {
            Palettes.replaceEquals(bitsPerEntry, values, Palettes.maxSize(dimension), oldValue, newValue);
            return;
        }
        final int oldIndex = table.indexOf(oldValue);
        if (oldIndex == -1 || table.countAt(oldIndex) == 0) return;
        final int newIndex = table.indexOf(newValue);
        if (newIndex == -1) {
            table.replaceValue(oldIndex, newValue);
            return;
        }
        Palettes.replaceEquals(bitsPerEntry, values, Palettes.maxSize(dimension), oldIndex, newIndex);
        table.moveAll(oldIndex, newIndex);
    }

    @Override
    public void setAll(EntrySupplier supplier) {
        final int dimension = this.dimension;
        int[] rawValues = null;
        int firstValue = 0;
        int index = 0;
        for (int y = 0; y < dimension; y++) {
            for (int z = 0; z < dimension; z++) {
                for (int x = 0; x < dimension; x++) {
                    final int value = supplier.get(x, y, z);
                    validateValue(value);
                    if (index == 0) {
                        firstValue = value;
                    } else if (rawValues == null && value != firstValue) {
                        rawValues = new int[Palettes.maxSize(dimension)];
                        Arrays.fill(rawValues, 0, index, firstValue);
                    }
                    if (rawValues != null) rawValues[index] = value;
                    index++;
                }
            }
        }
        if (rawValues == null) fill(firstValue);
        else store(this, rawValues);
    }

    @Override
    public void replace(int x, int y, int z, IntUnaryOperator operator) {
        final int dimension = this.dimension;
        validateCoord(dimension, x, y, z);
        final int bitsPerEntry = this.bitsPerEntry;
        final PaletteTable table = this.table;
        final int oldValue = isSingle(bitsPerEntry) ? singleValue : Palettes.readValue(
                dimension, bitsPerEntry, values, paletteValues(table), x, y, z);
        final int newValue = operator.applyAsInt(oldValue);
        validateValue(newValue);
        if (oldValue != newValue) setUnchecked(this, dimension, x, y, z, newValue);
    }

    @Override
    public void replaceAll(EntryFunction function) {
        final int dimension = this.dimension;
        int[] rawValues = null;
        final int bitsPerEntry = this.bitsPerEntry;
        final int singleValue = this.singleValue;
        final long[] values = this.values;
        final PaletteTable table = this.table;
        final int @Nullable [] paletteValues = paletteValues(table);
        int firstValue = 0;
        boolean changed = false;
        int index = 0;
        for (int y = 0; y < dimension; y++) {
            for (int z = 0; z < dimension; z++) {
                for (int x = 0; x < dimension; x++) {
                    final int oldValue = isSingle(bitsPerEntry) ? singleValue : Palettes.readValue(
                            dimension, bitsPerEntry, values, paletteValues, x, y, z);
                    final int value = function.apply(x, y, z, oldValue);
                    validateValue(value);
                    changed |= value != oldValue;
                    if (index == 0) {
                        firstValue = value;
                    } else if (rawValues == null && value != firstValue) {
                        rawValues = new int[Palettes.maxSize(dimension)];
                        Arrays.fill(rawValues, 0, index, firstValue);
                    }
                    if (rawValues != null) rawValues[index] = value;
                    index++;
                }
            }
        }
        if (!changed) return;
        if (rawValues == null) fill(firstValue);
        else store(this, rawValues);
    }

    private static void makeDirect(PaletteImpl palette, int directBits, int[] rawValues) {
        palette.bitsPerEntry = (byte) directBits;
        palette.table = null;
        palette.values = Palettes.pack(rawValues, directBits);
    }

    private static void store(PaletteImpl palette, int[] rawValues) {
        final int directBits = palette.directBits;
        final int maxBitsPerEntry = palette.maxBitsPerEntry;
        if (directBits > maxBitsPerEntry) makeDirect(palette, directBits, rawValues);
        else rebuild(palette, palette.minBitsPerEntry, maxBitsPerEntry, directBits, rawValues);
    }

    private static void rebuild(PaletteImpl palette, int minBitsPerEntry, int maxBitsPerEntry,
                                int directBits, int[] rawValues) {
        final int maxIndirectSize = Palettes.maxPaletteSize(maxBitsPerEntry);
        PaletteTable table = new PaletteTable(Palettes.maxPaletteSize(minBitsPerEntry));
        boolean direct = false;
        for (int index = 0; index < rawValues.length; index++) {
            final int value = rawValues[index];
            palette.validateValue(value);
            int paletteIndex = table.indexOf(value);
            if (paletteIndex == -1) {
                paletteIndex = table.insert(value);
                if (paletteIndex == -1) {
                    if (table.capacity() == maxIndirectSize) {
                        validateDirectAvailable(maxBitsPerEntry, directBits, maxIndirectSize);
                        for (int previous = 0; previous < index; previous++) {
                            rawValues[previous] = table.value(rawValues[previous]);
                        }
                        direct = true;
                        break;
                    }
                    table.grow(table.capacity() << 1);
                    paletteIndex = table.insert(value);
                }
            }
            table.addCount(paletteIndex, 1);
            rawValues[index] = paletteIndex;
        }

        if (direct) {
            makeDirect(palette, directBits, rawValues);
            return;
        }
        if (table.size() == 1) {
            palette.fill(table.value(0));
            return;
        }
        final byte bitsPerEntry = (byte) Math.max(minBitsPerEntry, Palettes.bitsToIndex(table.size()));
        palette.bitsPerEntry = bitsPerEntry;
        palette.table = table;
        palette.values = Palettes.pack(rawValues, bitsPerEntry);
    }

    private static void rebuildFrom(PaletteImpl palette, int dimension, int minBitsPerEntry,
                                    int maxBitsPerEntry, int directBits, int sourceBitsPerEntry,
                                    long[] sourceValues, int @Nullable [] sourcePaletteValues) {
        final int size = Palettes.maxSize(dimension);
        final int[] rawValues = Palettes.unpackValues(
                size, sourceBitsPerEntry, sourceValues, sourcePaletteValues);
        rebuild(palette, minBitsPerEntry, maxBitsPerEntry, directBits, rawValues);
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
            throw new IllegalArgumentException("Source palette dimension (" + sourcePalette.dimension
                    + ") must equal target palette dimension (" + dimension + ")");
        }
        final int sourceMinX = Math.max(0, -offsetX);
        final int sourceMinY = Math.max(0, -offsetY);
        final int sourceMinZ = Math.max(0, -offsetZ);
        final int sourceMaxX = Math.min(dimension, dimension - offsetX);
        final int sourceMaxY = Math.min(dimension, dimension - offsetY);
        final int sourceMaxZ = Math.min(dimension, dimension - offsetZ);
        if (sourceMinX >= sourceMaxX || sourceMinY >= sourceMaxY || sourceMinZ >= sourceMaxZ) return;

        final PaletteTable sourceTable = sourcePalette.table;
        if (isIndirect(sourceTable) && this != sourcePalette) {
            copyFromIndirect(this, dimension,
                    sourcePalette.bitsPerEntry, sourcePalette.values, sourceTable,
                    sourceMinX, sourceMinY, sourceMinZ, sourceMaxX, sourceMaxY, sourceMaxZ,
                    offsetX, offsetY, offsetZ);
            return;
        }

        for (int y = sourceMinY; y < sourceMaxY; y++) {
            for (int z = sourceMinZ; z < sourceMaxZ; z++) {
                for (int x = sourceMinX; x < sourceMaxX; x++) {
                    final int value = getUnchecked(sourcePalette, dimension, x, y, z);
                    validateValue(value);
                    setUnchecked(this, dimension, x + offsetX, y + offsetY, z + offsetZ, value);
                }
            }
        }
    }

    // Assumes BPE > 0
    private static void copyFromIndirect(PaletteImpl target, int dimension,
                                         int sourceBitsPerEntry, long[] sourceValues, PaletteTable sourceTable,
                                         int sourceMinX, int sourceMinY, int sourceMinZ,
                                         int sourceMaxX, int sourceMaxY, int sourceMaxZ,
                                         int offsetX, int offsetY, int offsetZ) {
        target.validateTableValues(sourceTable, 0);
        if (isDirect(target.bitsPerEntry, target.table)) {
            Palettes.copyIndirectToDirect(dimension,
                    target.bitsPerEntry, target.values,
                    sourceBitsPerEntry, sourceValues, sourceTable.values(),
                    sourceMinX, sourceMinY, sourceMinZ, sourceMaxX, sourceMaxY, sourceMaxZ,
                    offsetX, offsetY, offsetZ);
            return;
        }

        final int[] remapping = new int[sourceTable.size()];
        Arrays.fill(remapping, -1);
        PaletteTable targetTable = target.table;
        int[] countDeltas = isIndirect(targetTable) ? new int[targetTable.capacity()] : null;
        int targetBitsPerEntry = target.bitsPerEntry;
        long[] targetValues = target.values;
        for (int y = sourceMinY; y < sourceMaxY; y++) {
            for (int z = sourceMinZ; z < sourceMaxZ; z++) {
                for (int x = sourceMinX; x < sourceMaxX; x++) {
                    final int sourceIndex = Palettes.read(dimension, sourceBitsPerEntry, sourceValues, x, y, z);
                    int targetIndex = remapping[sourceIndex];
                    if (targetIndex == -1) {
                        targetIndex = target.valueToPaletteIndex(sourceTable.value(sourceIndex));
                        remapping[sourceIndex] = targetIndex;
                        targetTable = target.table;
                        targetBitsPerEntry = target.bitsPerEntry;
                        targetValues = target.values;
                        if (!isIndirect(targetTable)) {
                            Palettes.writeValue(dimension, targetBitsPerEntry, targetValues,
                                    x + offsetX, y + offsetY, z + offsetZ, sourceTable.value(sourceIndex));
                            continue;
                        }
                        final int capacity = targetTable.capacity();
                        if (countDeltas == null) countDeltas = new int[capacity];
                        else if (countDeltas.length < capacity) countDeltas = Arrays.copyOf(countDeltas, capacity);
                        if (targetTable.countAt(targetIndex) == 0) {
                            targetTable.addCount(targetIndex, 1);
                            countDeltas[targetIndex]--;
                        }
                    } else if (!isIndirect(targetTable)) {
                        Palettes.writeValue(dimension, targetBitsPerEntry, targetValues,
                                x + offsetX, y + offsetY, z + offsetZ, sourceTable.value(sourceIndex));
                        continue;
                    }
                    final int oldIndex = Palettes.write(dimension, targetBitsPerEntry, targetValues,
                            x + offsetX, y + offsetY, z + offsetZ, targetIndex);
                    if (oldIndex == targetIndex) continue;
                    countDeltas[oldIndex]--;
                    countDeltas[targetIndex]++;
                }
            }
        }
        if (isIndirect(targetTable)) {
            assert countDeltas != null;
            targetTable.addCounts(countDeltas);
        }
    }

    @Override
    public void copyFrom(Palette source) {
        final PaletteImpl sourcePalette = (PaletteImpl) source;
        final int dimension = this.dimension;
        if (sourcePalette.dimension != dimension) {
            throw new IllegalArgumentException("Source palette dimension (" + sourcePalette.dimension
                    + ") must equal target palette dimension (" + dimension + ")");
        }
        final int minBitsPerEntry = this.minBitsPerEntry;
        final int maxBitsPerEntry = this.maxBitsPerEntry;
        final int directBits = this.directBits;
        if (sourcePalette.minBitsPerEntry == minBitsPerEntry
                && sourcePalette.maxBitsPerEntry == maxBitsPerEntry
                && sourcePalette.directBits == directBits) {
            copySameConfiguration(this, sourcePalette);
            return;
        }
        copyDifferentConfiguration(this, sourcePalette, dimension,
                minBitsPerEntry, maxBitsPerEntry, directBits);
    }

    private static void copySameConfiguration(PaletteImpl target, PaletteImpl source) {
        final long @Nullable [] values = source.values;
        final @Nullable PaletteTable table = source.table;
        target.bitsPerEntry = source.bitsPerEntry;
        target.singleValue = source.singleValue;
        target.values = values == null ? null : values.clone();
        target.table = table == null ? null : table.clone();
    }

    private static void copyDifferentConfiguration(PaletteImpl target, PaletteImpl source, int dimension,
                                                   int minBitsPerEntry, int maxBitsPerEntry, int directBits) {
        final int sourceBitsPerEntry = source.bitsPerEntry;
        if (isSingle(sourceBitsPerEntry)) {
            target.fill(source.singleValue);
            return;
        }
        final long[] sourceValues = source.values;
        final PaletteTable sourceTable = source.table;
        final int @Nullable [] sourcePaletteValues = paletteValues(sourceTable);
        rebuildFrom(target, dimension, minBitsPerEntry, maxBitsPerEntry, directBits,
                sourceBitsPerEntry, sourceValues, sourcePaletteValues);
    }

    @Override
    public int count(int value) {
        final int size = Palettes.maxSize(this.dimension);
        final int bitsPerEntry = this.bitsPerEntry;
        if (isSingle(bitsPerEntry)) return singleValue == value ? size : 0;
        final PaletteTable table = this.table;
        if (isIndirect(table)) return table.count(value);
        return Palettes.countEquals(bitsPerEntry, values, size, value);
    }

    @Override
    public int count(IntPredicate predicate) {
        final int size = Palettes.maxSize(this.dimension);
        final int bitsPerEntry = this.bitsPerEntry;
        if (isSingle(bitsPerEntry)) return predicate.test(singleValue) ? size : 0;
        final PaletteTable table = this.table;
        if (isIndirect(table)) return table.count(predicate);
        return Palettes.countMatches(bitsPerEntry, values, size, null, predicate);
    }

    @Override
    public void getAllCounts(ValueCountConsumer consumer) {
        final int size = Palettes.maxSize(this.dimension);
        final int bitsPerEntry = this.bitsPerEntry;
        if (isSingle(bitsPerEntry)) {
            consumer.accept(singleValue, size);
            return;
        }
        final PaletteTable table = this.table;
        if (isIndirect(table)) {
            table.getAllCounts(consumer);
            return;
        }
        Palettes.getAllCounts(bitsPerEntry, values, size, consumer);
    }

    @Override
    public boolean any(int value) {
        final int bitsPerEntry = this.bitsPerEntry;
        if (isSingle(bitsPerEntry)) return singleValue == value;
        final PaletteTable table = this.table;
        if (isIndirect(table)) return table.count(value) != 0;
        return Palettes.anyEquals(bitsPerEntry, values, Palettes.maxSize(this.dimension), value);
    }

    @Override
    public boolean any(IntPredicate predicate) {
        final int bitsPerEntry = this.bitsPerEntry;
        if (isSingle(bitsPerEntry)) return predicate.test(singleValue);
        final PaletteTable table = this.table;
        return isIndirect(table)
                ? table.any(predicate)
                : Palettes.anyMatch(bitsPerEntry, values, Palettes.maxSize(this.dimension), null, predicate);
    }

    @Override
    public boolean all(int value) {
        final int bitsPerEntry = this.bitsPerEntry;
        if (isSingle(bitsPerEntry)) return singleValue == value;
        final int size = Palettes.maxSize(this.dimension);
        final PaletteTable table = this.table;
        if (isIndirect(table)) return table.count(value) == size;
        return Palettes.allEquals(bitsPerEntry, values, size, value);
    }

    @Override
    public boolean all(IntPredicate predicate) {
        final int bitsPerEntry = this.bitsPerEntry;
        if (isSingle(bitsPerEntry)) return predicate.test(singleValue);
        final PaletteTable table = this.table;
        return isIndirect(table)
                ? table.all(predicate)
                : Palettes.allMatch(bitsPerEntry, values, Palettes.maxSize(this.dimension), null, predicate);
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
        if (isSingle(bitsPerEntry)) return;
        final int dimension = this.dimension;
        final int directBits = this.directBits;
        final long[] values = this.values;
        final PaletteTable table = this.table;
        if (focus == Optimization.SPEED) {
            // Direct storage is unavailable when the whole value space already fits indirect storage
            if (directBits > maxBitsPerEntry) makeDirect(this, dimension, directBits, bitsPerEntry, values, table);
            return;
        }
        if (isIndirect(table)) {
            final int liveIndex = table.singleLiveIndex();
            if (liveIndex != -1) {
                fill(table.value(liveIndex));
                return;
            }
        }
        final int @Nullable [] paletteValues = paletteValues(table);
        rebuildFrom(this, dimension, minBitsPerEntry, maxBitsPerEntry, directBits,
                bitsPerEntry, values, paletteValues);
    }

    @Override
    public boolean compare(Palette other) {
        if (this == other) return true;
        final PaletteImpl palette = (PaletteImpl) other;
        final int dimension = this.dimension;
        if (palette.dimension != dimension) return false;
        final int firstBitsPerEntry = this.bitsPerEntry;
        final int firstSingleValue = this.singleValue;
        final long[] firstValues = this.values;
        final PaletteTable firstTable = this.table;
        final int secondBitsPerEntry = palette.bitsPerEntry;
        final int secondSingleValue = palette.singleValue;
        final long[] secondValues = palette.values;
        final PaletteTable secondTable = palette.table;
        if (isSingle(firstBitsPerEntry) && isSingle(secondBitsPerEntry)) return firstSingleValue == secondSingleValue;
        final int @Nullable [] firstPaletteValues = paletteValues(firstTable);
        final int @Nullable [] secondPaletteValues = paletteValues(secondTable);
        int firstValue = isSingle(firstBitsPerEntry) ? firstSingleValue :
                (int) firstValues[0] & (1 << firstBitsPerEntry) - 1;
        if (firstPaletteValues != null) firstValue = firstPaletteValues[firstValue];
        int secondValue = isSingle(secondBitsPerEntry) ? secondSingleValue :
                (int) secondValues[0] & (1 << secondBitsPerEntry) - 1;
        if (secondPaletteValues != null) secondValue = secondPaletteValues[secondValue];
        if (firstValue != secondValue) return false;
        return Palettes.compare(Palettes.maxSize(dimension), 1, // we check 0 above, as a fast check
                firstBitsPerEntry, firstSingleValue, firstValues, firstPaletteValues,
                secondBitsPerEntry, secondSingleValue, secondValues,
                secondPaletteValues);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Palette clone() {
        final PaletteImpl clone = new PaletteImpl(dimension, minBitsPerEntry, maxBitsPerEntry, directBits);
        clone.bitsPerEntry = bitsPerEntry;
        clone.singleValue = singleValue;
        clone.values = values == null ? null : values.clone();
        clone.table = table == null ? null : table.clone();
        return clone;
    }

    private static void makeDirect(PaletteImpl palette, int dimension, int directBits,
                                   int bitsPerEntry, long[] values, @Nullable PaletteTable table) {
        if (!isIndirect(table)) return;
        palette.validateTableValues(table, 0);
        palette.values = Palettes.remap(dimension, bitsPerEntry, directBits, values, table::value);
        palette.table = null;
        palette.bitsPerEntry = (byte) directBits;
    }

    private static @Nullable PaletteTable upsize(PaletteImpl palette, int dimension, int oldBits,
                                                 long[] values, PaletteTable table) {
        final byte newBits = (byte) (oldBits + 1);
        final int maxBitsPerEntry = palette.maxBitsPerEntry;
        if (newBits > maxBitsPerEntry) {
            validateDirectAvailable(maxBitsPerEntry, palette.directBits, Palettes.maxPaletteSize(maxBitsPerEntry));
            makeDirect(palette, dimension, palette.directBits, oldBits, values, table);
            return null;
        }
        palette.values = Palettes.remap(dimension, oldBits, newBits, values, value -> value);
        table.grow(Palettes.maxPaletteSize(newBits));
        palette.bitsPerEntry = newBits;
        return table;
    }

    private static PaletteTable initIndirect(PaletteImpl palette, int dimension, int bitsPerEntry,
                                             long[] values) {
        final PaletteTable table = new PaletteTable(Palettes.maxPaletteSize(bitsPerEntry));
        table.insert(palette.singleValue, Palettes.maxSize(dimension));
        palette.bitsPerEntry = (byte) bitsPerEntry;
        palette.values = values;
        palette.table = table;
        return table;
    }

    @Override
    public int paletteIndexToValue(int value) {
        final PaletteTable table = this.table;
        return isIndirect(table) ? table.value(value) : value;
    }

    @Override
    public int valueToPaletteIndex(int value) {
        validateValue(value);
        final int dimension = this.dimension;
        int bitsPerEntry = this.bitsPerEntry;
        long[] values = this.values;
        PaletteTable table = this.table;
        if (isDirect(bitsPerEntry, table)) return value;
        if (isSingle(bitsPerEntry)) {
            bitsPerEntry = minBitsPerEntry;
            values = new long[arrayLength(dimension, bitsPerEntry)];
            table = initIndirect(this, dimension, bitsPerEntry, values);
        }
        int index = table.indexOf(value);
        if (index != -1) return index;
        index = table.insert(value);
        if (index != -1) return index;
        table = upsize(this, dimension, bitsPerEntry, values, table);
        if (!isIndirect(table)) return value;
        index = table.insert(value);
        assert index != -1;
        return index;
    }

    @Override
    public int singleValue() {
        return singleValue;
    }

    @Override
    public long @Nullable [] indexedValues() {
        final long[] values = this.values;
        return values == null ? null : values.clone();
    }

    private void validateValue(int value) {
        if (value < 0 || value >= 1L << directBits) {
            throw new IllegalArgumentException("Palette value must fit the direct width " + directBits
                    + ", got " + value);
        }
    }

    private void validateTableValues(PaletteTable table, int offset) {
        for (int index = 0; index < table.size(); index++) {
            if (table.countAt(index) != 0) validateValue(table.value(index) + offset);
        }
    }

    private void validateValues(int[] values) {
        for (int value : values) validateValue(value);
    }

    private static void validateCoord(int dimension, int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0) throw new IllegalArgumentException("Coordinates must be non-negative");
        if (x >= dimension || y >= dimension || z >= dimension) {
            throw new IllegalArgumentException("Coordinates must be less than the dimension size, got "
                    + x + ", " + y + ", " + z + " for dimension " + dimension);
        }
    }

    private static void validateDimension(int dimension) {
        if (dimension <= 1 || (dimension & dimension - 1) != 0) {
            throw new IllegalArgumentException("Dimension must be a positive power of 2, got " + dimension);
        }
    }

    private static void validateConfiguration(int minBitsPerEntry, int maxBitsPerEntry, int directBits) {
        if (minBitsPerEntry < 1 || minBitsPerEntry > maxBitsPerEntry || maxBitsPerEntry > 30) {
            throw new IllegalArgumentException("Indirect widths must satisfy 1 <= min <= max <= 30, got ["
                    + minBitsPerEntry + ", " + maxBitsPerEntry + "]");
        }
        if (directBits < 1 || directBits > 31) {
            throw new IllegalArgumentException("Direct width must be within [1, 31], got " + directBits);
        }
    }

    /// Single value mode, every entry has the same value and no array is allocated.
    private static boolean isSingle(int bitsPerEntry) {
        return bitsPerEntry == 0;
    }

    /// Indirect mode, the packed array stores indices into `table`.
    @Contract("null -> false")
    private static boolean isIndirect(@Nullable PaletteTable table) {
        return table != null;
    }

    /// Direct mode, the packed array stores values and has no lookup structure.
    @Contract("_, !null -> false")
    private static boolean isDirect(int bitsPerEntry, @Nullable PaletteTable table) {
        return !isSingle(bitsPerEntry) && !isIndirect(table);
    }

    private static int @Nullable [] paletteValues(@Nullable PaletteTable table) {
        return isIndirect(table) ? table.values() : null;
    }

    private static void validateDirectAvailable(int maxBitsPerEntry, int directBits, int maxIndirectSize) {
        if (directBits > maxBitsPerEntry) return;
        throw new IllegalArgumentException("Palette cannot hold more than " + maxIndirectSize
                + " distinct values, its direct width (" + directBits
                + ") does not exceed its indirect width (" + maxBitsPerEntry + ")");
    }

    private static void validateBitsPerEntry(int minBitsPerEntry, int maxBitsPerEntry, int directBits,
                                             int bitsPerEntry) {
        if (isSingle(bitsPerEntry)) return;
        if (bitsPerEntry >= minBitsPerEntry && bitsPerEntry <= maxBitsPerEntry) return;
        if (bitsPerEntry == directBits) return;
        throw new IllegalArgumentException("Bits per entry must be 0, within [" + minBitsPerEntry + ", "
                + maxBitsPerEntry + "], or the direct width " + directBits + ", got " + bitsPerEntry);
    }
}
