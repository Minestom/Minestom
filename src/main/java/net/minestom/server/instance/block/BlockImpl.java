package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

record BlockImpl(RegistryData.BlockEntry registry,
                 long propertiesArray,
                 @Nullable CompoundBinaryTag nbt,
                 @Nullable BlockHandler handler) implements Block {
    /**
     * Number of bits used to store the index of a property value.
     * <p>
     * Block states are all stored within a single number.
     */
    private static final int BITS_PER_INDEX = 5;

    private static final int MAX_STATES = Long.SIZE / BITS_PER_INDEX;
    private static final int MAX_VALUES = 1 << BITS_PER_INDEX;

    private static final int HASH_VALUES_THRESHOLD = 8;

    // Block state -> block object
    private static final List<Block> BLOCK_STATE_MAP;
    private static final List<BlockSchema> BLOCK_SCHEMAS;
    static final Registry<Block> REGISTRY;

    static {
        //TODO compute default sizes from the registry data
        ObjectArray<Block> blockStateMap = ObjectArray.singleThread();
        ObjectArray<BlockSchema> blockSchemas = ObjectArray.singleThread();
        HashMap<Object, Object> internCache = new HashMap<>();
        // Most blocks reuse a small set of property definitions and complete layouts.
        Map<PropertyTypeKey, PropertyType> propertyTypeCache = new HashMap<>();
        Map<List<PropertyType>, PropertyType[]> propertyLayoutCache = new HashMap<>();

        REGISTRY = RegistryData.createStaticRegistry(
                BuiltinRegistries.BLOCK,
                (namespace, properties) -> {
                    final int blockId = properties.getInt("id");
                    final RegistryData.Properties stateObject = properties.section("states");
                    Objects.requireNonNull(stateObject);

                    // Retrieve properties
                    PropertyType[] propertyTypes;
                    {
                        RegistryData.Properties stateProperties = properties.section("properties");
                        if (stateProperties != null) {
                            final int stateCount = stateProperties.size();
                            if (stateCount > MAX_STATES) {
                                throw new IllegalStateException("Too many properties for block " + namespace);
                            }
                            propertyTypes = new PropertyType[stateCount];
                            int i = 0;
                            for (var entry : stateProperties) {
                                final var k = entry.getKey();
                                @SuppressWarnings("unchecked")
                                final var v = (List<String>) entry.getValue();
                                assert v.size() < MAX_VALUES;
                                final PropertyTypeKey propertyTypeKey = new PropertyTypeKey(k, v);
                                propertyTypes[i++] = propertyTypeCache.computeIfAbsent(propertyTypeKey,
                                        key -> new PropertyType(key.key, key.values));
                            }
                        } else {
                            propertyTypes = new PropertyType[0];
                        }
                        final List<PropertyType> propertyLayout = List.of(propertyTypes);
                        propertyTypes = propertyLayoutCache.computeIfAbsent(propertyLayout,
                                layout -> layout.toArray(PropertyType[]::new));
                    }

                    final RegistryData.BlockEntry baseBlockEntry = RegistryData.block(namespace, properties, internCache, null, null);

                    // Retrieve block states
                    {
                        final int propertiesCount = stateObject.size();
                        final int expectedStateCount = BlockSchema.stateCount(propertyTypes);
                        if (expectedStateCount != propertiesCount) {
                            throw new IllegalStateException("Invalid state count for block " + namespace +
                                    ": expected " + expectedStateCount + ", got " + propertiesCount);
                        }
                        final BlockSchema loadingSchema = new BlockSchema(propertyTypes, 0, expectedStateCount, null);
                        // Transient mapping used to compact the retained schema after registry loading.
                        final int[] stateIds = new int[expectedStateCount];
                        Arrays.fill(stateIds, -1);
                        for (var stateEntry : stateObject) {
                            final String query = stateEntry.getKey();
                            @SuppressWarnings("unchecked")
                            final var stateOverride = (Map<String, Object>) stateEntry.getValue();
                            final var propertyMap = BlockUtils.parseProperties(query);
                            if (propertyTypes.length != propertyMap.size()) {
                                throw new IllegalStateException("Invalid property count for block state " + namespace + query);
                            }
                            long propertiesValue = 0;
                            for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
                                final byte keyIndex = loadingSchema.findKeyIndexThrow(entry.getKey(), null);
                                final byte valueIndex = loadingSchema.findValueIndexThrow(propertyTypes[keyIndex], entry.getValue(), null);
                                propertiesValue = updateIndex(propertiesValue, keyIndex, valueIndex);
                            }

                            final RegistryData.BlockEntry entryOverride = RegistryData.block(namespace, RegistryData.Properties.fromMap(stateOverride), internCache, baseBlockEntry, properties);
                            final BlockImpl block = new BlockImpl(entryOverride,
                                    propertiesValue, null, null);
                            blockStateMap.set(block.stateId(), block);
                            final int stateIndex = loadingSchema.stateIndex(propertiesValue);
                            if (stateIds[stateIndex] != -1) {
                                throw new IllegalStateException("Duplicate properties for block state " + namespace + query);
                            }
                            stateIds[stateIndex] = block.stateId();
                        }
                        blockSchemas.set(blockId, propertyTypes.length == 0 ? BlockSchema.EMPTY :
                                BlockSchema.create(namespace, propertyTypes, stateIds));
                    }
                    // Register default state
                    final int defaultState = properties.getInt("defaultStateId");
                    return blockStateMap.get(defaultState);
                });
        BLOCK_STATE_MAP = blockStateMap.toList();
        BLOCK_SCHEMAS = blockSchemas.toList();
    }

    static @UnknownNullability Block get(RegistryKey<Block> key) {
        return REGISTRY.get(key);
    }

    static int statesCount() {
        return BLOCK_STATE_MAP.size();
    }

    static Block getState(int stateId) {
        return BLOCK_STATE_MAP.get(stateId);
    }

    @SuppressWarnings("PatternValidation")
    static @Nullable Block parseState(String input) {
        if (input.isEmpty()) return null;
        final int nbtIndex = input.indexOf("[");
        if (nbtIndex == 0) return null;
        if (nbtIndex == -1) return Block.fromKey(input);
        if (!input.endsWith("]")) return null;
        // Block state
        final String blockName = input.substring(0, nbtIndex);
        Block block = Block.fromKey(blockName);
        if (block == null) return null;
        // Compute properties
        final String query = input.substring(nbtIndex);
        final Map<String, String> propertyMap = BlockUtils.parseProperties(query);
        try {
            return block.withProperties(propertyMap);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public Block withProperty(String property, String value) {
        final BlockSchema schema = schema();
        final byte keyIndex = schema.findKeyIndexThrow(property, this);
        final byte valueIndex = schema.findValueIndexThrow(schema.properties[keyIndex], value, this);
        final long updatedProperties = updateIndex(propertiesArray, keyIndex, valueIndex);
        return compute(updatedProperties);
    }

    @Override
    public Block withProperties(Map<String, String> properties) {
        if (properties.isEmpty()) return this;
        final BlockSchema schema = schema();
        long updatedProperties = this.propertiesArray;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            final byte keyIndex = schema.findKeyIndexThrow(entry.getKey(), this);
            final byte valueIndex = schema.findValueIndexThrow(schema.properties[keyIndex], entry.getValue(), this);
            updatedProperties = updateIndex(updatedProperties, keyIndex, valueIndex);
        }
        return compute(updatedProperties);
    }

    @Override
    public <T> Block withTag(Tag<T> tag, @Nullable T value) {
        var builder = CompoundBinaryTag.builder();
        if (nbt != null) builder.put(nbt);
        tag.write(builder, value);
        final CompoundBinaryTag temporaryNbt = builder.build();
        final CompoundBinaryTag finalNbt = !temporaryNbt.isEmpty() ? temporaryNbt : null;
        return withNbt(finalNbt);
    }

    @Override
    public Block withNbt(@Nullable CompoundBinaryTag compound) {
        if (nbt == compound) return this;
        return copy(compound, handler);
    }

    @Override
    public Block withHandler(@Nullable BlockHandler handler) {
        if (this.handler == handler) return this;
        return copy(nbt, handler);
    }

    @Override
    public @Unmodifiable Map<String, String> properties() {
        return createPropertiesMap(schema().properties, propertiesArray);
    }

    @Override
    public String state() {
        return createStateString(name(), schema().properties, propertiesArray);
    }

    @Override
    public Block defaultState() {
        return Objects.requireNonNull(Block.fromBlockId(id()));
    }

    @Override
    public @Nullable String getProperty(String property) {
        final BlockSchema schema = schema();
        if (schema.properties.length == 0) return null;
        final int key = schema.findKeyIndex(property);
        if (key == -1) return null; // Property not found
        final long index = extractIndex(propertiesArray, key);
        return schema.properties[key].values().get((int) index);
    }

    @Override
    public Collection<Block> possibleStates() {
        final BlockSchema schema = schema();
        if (schema.properties.length == 0) return List.of(defaultState());
        return new AbstractList<>() {
            @Override
            public Block get(int index) {
                Objects.checkIndex(index, schema.stateCount);
                return BLOCK_STATE_MAP.get(schema.firstStateId + index);
            }

            @Override
            public int size() {
                return schema.stateCount;
            }
        };
    }

    @Override
    public <T> @UnknownNullability T getTag(Tag<T> tag) {
        return tag.read(Objects.requireNonNullElse(nbt, CompoundBinaryTag.empty()));
    }

    private BlockSchema schema() {
        return BLOCK_SCHEMAS.get(id());
    }

    private Block copy(@Nullable CompoundBinaryTag nbt, @Nullable BlockHandler handler) {
        // Plain states always use the canonical registry instance.
        if (nbt == null && handler == null) return BLOCK_STATE_MAP.get(stateId());
        return new BlockImpl(registry, propertiesArray, nbt, handler);
    }

    @Override
    public String toString() {
        return name() + "{properties=" + properties() + ", nbt=" + nbt + ", handler=" + handler + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockImpl block)) return false;
        return stateId() == block.stateId() && Objects.equals(nbt, block.nbt) && Objects.equals(handler, block.handler);
    }

    @Override
    public int hashCode() {
        int result = stateId();
        result = 31 * result + Objects.hashCode(nbt);
        result = 31 * result + Objects.hashCode(handler);
        return result;
    }

    private Block compute(long updatedProperties) {
        if (updatedProperties == this.propertiesArray) return this;
        final Block block = schema().state(updatedProperties);
        // Reuse the same block instance if possible
        if (nbt == null && handler == null) return block;
        // Otherwise copy with the nbt and handler
        return new BlockImpl(block.registry(), updatedProperties, nbt, handler);
    }

    private static Map<String, String> createPropertiesMap(PropertyType[] properties, long propertiesArray) {
        final int length = properties.length;
        if (length == 0) return Map.of();
        String[] keys = new String[length];
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            PropertyType property = properties[i];
            keys[i] = property.key();
            values[i] = property.values().get((int) extractIndex(propertiesArray, i));
        }
        return Object2ObjectMaps.unmodifiable(new Object2ObjectArrayMap<>(keys, values, length));
    }

    private static String createStateString(String namespace, PropertyType[] properties, long propertiesArray) {
        if (properties.length == 0) return namespace;
        StringBuilder builder = new StringBuilder(namespace).append('[');
        for (int i = 0; i < properties.length; i++) {
            if (i != 0) builder.append(',');
            final PropertyType property = properties[i];
            builder.append(property.key()).append('=')
                    .append(property.values().get((int) extractIndex(propertiesArray, i)));
        }
        return builder.append(']').toString();
    }

    private record BlockSchema(PropertyType[] properties, int firstStateId, int stateCount,
                               short @Nullable [] stateOffsets) {
        // Propertyless blocks need neither a state table nor a distinct schema.
        private static final BlockSchema EMPTY = new BlockSchema(new PropertyType[0], -1, 1, null);

        private static BlockSchema create(String namespace, PropertyType[] properties, int[] stateIds) {
            final int firstStateId = Arrays.stream(stateIds).min().orElseThrow();
            if (stateIds.length > 1 << Short.SIZE) {
                throw new IllegalStateException("Too many block states for compact lookup: " + namespace);
            }
            final boolean[] assignedOffsets = new boolean[stateIds.length];
            boolean identityOrder = true;
            for (int stateIndex = 0; stateIndex < stateIds.length; stateIndex++) {
                final int offset = stateIds[stateIndex] - firstStateId;
                if (offset < 0 || offset >= stateIds.length || assignedOffsets[offset]) {
                    throw new IllegalStateException("Block state ids are not contiguous for " + namespace);
                }
                assignedOffsets[offset] = true;
                identityOrder &= offset == stateIndex;
            }
            // Retain a permutation only when property order differs from state id order.
            if (identityOrder) return new BlockSchema(properties, firstStateId, stateIds.length, null);
            final short[] stateOffsets = new short[stateIds.length];
            for (int i = 0; i < stateIds.length; i++) stateOffsets[i] = (short) (stateIds[i] - firstStateId);
            return new BlockSchema(properties, firstStateId, stateIds.length, stateOffsets);
        }

        private static int stateCount(PropertyType[] properties) {
            int stateCount = 1;
            for (PropertyType property : properties) {
                stateCount = Math.multiplyExact(stateCount, property.values.size());
            }
            return stateCount;
        }

        private byte findKeyIndex(String key) {
            for (byte i = 0; i < properties.length; i++) {
                if (properties[i].key().equals(key)) return i;
            }
            return -1;
        }

        private byte findKeyIndexThrow(String key, @Nullable BlockImpl block) {
            final byte index = findKeyIndex(key);
            if (index == -1) {
                if (block != null) {
                    throw new IllegalArgumentException("Property " + key + " is not valid for block " + block);
                } else {
                    throw new IllegalArgumentException("Unknown property key: " + key);
                }
            }
            return index;
        }

        private byte findValueIndexThrow(PropertyType propertyType, String value, @Nullable BlockImpl block) {
            final byte index = propertyType.findValueIndex(value);
            if (index == -1) {
                if (block != null) {
                    throw new IllegalArgumentException("Property " + propertyType.key() + " value " + value + " is not valid for block " + block);
                } else {
                    throw new IllegalArgumentException("Unknown property value: " + value);
                }
            }
            return index;
        }

        private Block state(long propertiesArray) {
            final int stateIndex = stateIndex(propertiesArray);
            final int stateOffset = stateOffsets == null ? stateIndex : Short.toUnsignedInt(stateOffsets[stateIndex]);
            return BLOCK_STATE_MAP.get(firstStateId + stateOffset);
        }

        private int stateIndex(long propertiesArray) {
            // Convert the packed property indexes to an index in their Cartesian product.
            int index = 0;
            int stride = 1;
            for (int i = 0; i < properties.length; i++) {
                index += (int) extractIndex(propertiesArray, i) * stride;
                stride *= properties[i].values.size();
            }
            return index;
        }
    }

    private record PropertyType(String key, List<String> values, @Nullable Object2ByteMap<String> valueIndexes) {
        private PropertyType(String key, List<String> values) {
            this(key, values, valueIndexes(values));
        }

        private static @Nullable Object2ByteMap<String> valueIndexes(List<String> values) {
            if (values.size() < HASH_VALUES_THRESHOLD) return null;
            final Object2ByteOpenHashMap<String> indexes = new Object2ByteOpenHashMap<>(values.size());
            indexes.defaultReturnValue((byte) -1);
            for (byte i = 0; i < values.size(); i++) indexes.put(values.get(i), i);
            return indexes;
        }

        private byte findValueIndex(String value) {
            return valueIndexes != null ? valueIndexes.getByte(value) : (byte) values.indexOf(value);
        }
    }

    private record PropertyTypeKey(String key, List<String> values) {
    }

    static long updateIndex(long value, int index, byte newValue) {
        final int position = index * BITS_PER_INDEX;
        final int mask = (1 << BITS_PER_INDEX) - 1;
        value &= ~((long) mask << position); // Clear the bits at the specified position
        value |= (long) (newValue & mask) << position; // Set the new bits
        return value;
    }

    static long extractIndex(long value, int index) {
        final int position = index * BITS_PER_INDEX;
        final int mask = (1 << BITS_PER_INDEX) - 1;
        return ((value >> position) & mask);
    }
}
