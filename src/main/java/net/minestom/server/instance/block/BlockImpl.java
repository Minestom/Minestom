package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
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

    private static final int HASH_STATES_THRESHOLD = 16;

    // Block state -> block object
    private static final List<Block> BLOCK_STATE_MAP;
    private static final List<StateData> STATE_DATA_MAP;
    private static final List<BlockSchema> BLOCK_SCHEMAS;
    static final Registry<Block> REGISTRY;

    static {
        //TODO compute default sizes from the registry data
        ObjectArray<Block> blockStateMap = ObjectArray.singleThread();
        ObjectArray<StateData> stateDataMap = ObjectArray.singleThread();
        ObjectArray<BlockSchema> blockSchemas = ObjectArray.singleThread();
        HashMap<Object, Object> internCache = new HashMap<>();

        REGISTRY = RegistryData.createStaticRegistry(
                Key.key("block"),
                (namespace, properties) -> {
                    final int blockId = properties.getInt("id");
                    final RegistryData.Properties stateObject = properties.section("states");

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
                                final var v = (List<String>) entry.getValue();
                                assert v.size() < MAX_VALUES;
                                propertyTypes[i++] = new PropertyType(k, v);
                            }
                        } else {
                            propertyTypes = new PropertyType[0];
                        }
                    }

                    final RegistryData.BlockEntry baseBlockEntry = RegistryData.block(namespace, properties, internCache, null, null);

                    // Retrieve block states
                    {
                        final int propertiesCount = stateObject.size();
                        long[] propertiesKeys = new long[propertiesCount];
                        BlockImpl[] blocksValues = new BlockImpl[propertiesCount];
                        int propertiesOffset = 0;
                        BlockSchema schema = null;
                        for (var stateEntry : stateObject) {
                            final String query = stateEntry.getKey();
                            final var stateOverride = (Map<String, Object>) stateEntry.getValue();
                            final var propertyMap = BlockUtils.parseProperties(query);
                            if (propertyTypes.length != propertyMap.size()) {
                                throw new IllegalStateException("Invalid property count for block state " + namespace + query);
                            }
                            if (schema == null)
                                schema = new BlockSchema(propertyTypes, List.of(), new Long2ObjectArrayMap<>());
                            long propertiesValue = 0;
                            for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
                                final byte keyIndex = schema.findKeyIndexThrow(entry.getKey(), null);
                                final byte valueIndex = schema.findValueIndexThrow(propertyTypes[keyIndex], entry.getValue(), null);
                                propertiesValue = updateIndex(propertiesValue, keyIndex, valueIndex);
                            }

                            final RegistryData.BlockEntry entryOverride = RegistryData.block(namespace, RegistryData.Properties.fromMap(stateOverride), internCache, baseBlockEntry, properties);
                            final BlockImpl block = new BlockImpl(entryOverride,
                                    propertiesValue, null, null);
                            blockStateMap.set(block.stateId(), block);
                            stateDataMap.set(block.stateId(), new StateData(createPropertiesMap(propertyTypes, propertiesValue),
                                    createStateString(namespace, propertyTypes, propertiesValue)));
                            propertiesKeys[propertiesOffset] = propertiesValue;
                            blocksValues[propertiesOffset++] = block;
                        }
                        final Long2ObjectMap<BlockImpl> states = createStateLookup(propertiesKeys, blocksValues, propertiesOffset);
                        final List<Block> possibleStates = List.of(Arrays.copyOf(blocksValues, propertiesOffset, Block[].class));
                        blockSchemas.set(blockId, new BlockSchema(propertyTypes, possibleStates, states));
                    }
                    // Register default state
                    final int defaultState = properties.getInt("defaultStateId");
                    return blockStateMap.get(defaultState);
                });
        BLOCK_STATE_MAP = blockStateMap.toList();
        STATE_DATA_MAP = stateDataMap.toList();
        BLOCK_SCHEMAS = blockSchemas.toList();
    }

    static @UnknownNullability Block get(String key) {
        return REGISTRY.get(Key.key(key));
    }

    static int statesCount() {
        return BLOCK_STATE_MAP.size();
    }

    static Block getState(int stateId) {
        return BLOCK_STATE_MAP.get(stateId);
    }

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
        final CompoundBinaryTag finalNbt = temporaryNbt.size() > 0 ? temporaryNbt : null;
        return new BlockImpl(registry, propertiesArray, finalNbt, handler);
    }

    @Override
    public Block withNbt(@Nullable CompoundBinaryTag compound) {
        return new BlockImpl(registry, propertiesArray, compound, handler);
    }

    @Override
    public Block withHandler(@Nullable BlockHandler handler) {
        return new BlockImpl(registry, propertiesArray, nbt, handler);
    }

    @Override
    public @Unmodifiable Map<String, String> properties() {
        return stateData().properties;
    }

    @Override
    public String state() {
        return stateData().state;
    }

    @Override
    public Block defaultState() {
        return Block.fromBlockId(id());
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
        return schema().possibleStates;
    }

    @Override
    public <T> @UnknownNullability T getTag(Tag<T> tag) {
        return tag.read(Objects.requireNonNullElse(nbt, CompoundBinaryTag.empty()));
    }

    private BlockSchema schema() {
        return BLOCK_SCHEMAS.get(id());
    }

    private StateData stateData() {
        return STATE_DATA_MAP.get(stateId());
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
        final BlockImpl block = schema().states.get(updatedProperties);
        if (block == null)
            throw new IllegalStateException("No block state for " + name() + " properties " + updatedProperties);
        // Reuse the same block instance if possible
        if (nbt == null && handler == null) return block;
        // Otherwise copy with the nbt and handler
        return new BlockImpl(block.registry(), block.propertiesArray, nbt, handler);
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

    private static Long2ObjectMap<BlockImpl> createStateLookup(long[] keys, BlockImpl[] values, int size) {
        if (size < HASH_STATES_THRESHOLD) return new Long2ObjectArrayMap<>(keys, values, size);
        final Long2ObjectOpenHashMap<BlockImpl> lookup = new Long2ObjectOpenHashMap<>(size);
        for (int i = 0; i < size; i++) lookup.put(keys[i], values[i]);
        return lookup;
    }

    private record BlockSchema(PropertyType[] properties, List<Block> possibleStates,
                               Long2ObjectMap<BlockImpl> states, Object2ByteMap<String> keyIndexes) {

        private BlockSchema(PropertyType[] properties, List<Block> possibleStates, Long2ObjectMap<BlockImpl> states) {
            this(properties, possibleStates, states, keyIndexes(properties));
        }

        private static Object2ByteMap<String> keyIndexes(PropertyType[] properties) {
            final Object2ByteOpenHashMap<String> propertyIndexes = new Object2ByteOpenHashMap<>(properties.length);
            propertyIndexes.defaultReturnValue((byte) -1);
            for (byte i = 0; i < properties.length; i++) {
                propertyIndexes.put(properties[i].key(), i);
            }
            return propertyIndexes;
        }

        private byte findKeyIndex(String key) {
            return keyIndexes.getByte(key);
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
            final byte index = propertyType.valueIndexes().getByte(value);
            if (index == -1) {
                if (block != null) {
                    throw new IllegalArgumentException("Property " + propertyType.key() + " value " + value + " is not valid for block " + block);
                } else {
                    throw new IllegalArgumentException("Unknown property value: " + value);
                }
            }
            return index;
        }
    }

    private record StateData(Map<String, String> properties, String state) {
    }

    private record PropertyType(String key, List<String> values, Object2ByteMap<String> valueIndexes) {
        private PropertyType(String key, List<String> values) {
            this(key, values, valueIndexes(values));
        }

        private static Object2ByteMap<String> valueIndexes(List<String> values) {
            final Object2ByteOpenHashMap<String> indexes = new Object2ByteOpenHashMap<>(values.size());
            indexes.defaultReturnValue((byte) -1);
            for (byte i = 0; i < values.size(); i++) indexes.put(values.get(i), i);
            return indexes;
        }
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
