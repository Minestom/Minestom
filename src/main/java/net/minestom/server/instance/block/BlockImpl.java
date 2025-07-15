package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.collection.ObjectArray;
import org.jspecify.annotations.Nullable;
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

    // Block state -> block object
    private static final List<Block> BLOCK_STATE_MAP;
    // Block id -> valid property keys (order is important for lookup)
    private static final List<PropertyType[]> PROPERTIES_TYPE;
    // Block id -> Map<Properties, Block>
    private static final List<Long2ObjectArrayMap<BlockImpl>> POSSIBLE_STATES;
    static final Registry<Block> REGISTRY;

    static {
        //TODO compute default sizes from the registry data
        ObjectArray<Block> blockStateMap = ObjectArray.singleThread();
        ObjectArray<PropertyType[]> propertiesType = ObjectArray.singleThread();
        ObjectArray<Long2ObjectArrayMap<BlockImpl>> possibleStates = ObjectArray.singleThread();
        HashMap<Object, Object> internCache = new HashMap<>();

        REGISTRY = RegistryData.createStaticRegistry(
                Key.key("minecraft:block"),
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
                    propertiesType.set(blockId, propertyTypes);

                    final RegistryData.BlockEntry baseBlockEntry = RegistryData.block(namespace, properties, internCache, null, null);

                    // Retrieve block states
                    {
                        final int propertiesCount = stateObject.size();
                        long[] propertiesKeys = new long[propertiesCount];
                        BlockImpl[] blocksValues = new BlockImpl[propertiesCount];
                        int propertiesOffset = 0;
                        for (var stateEntry : stateObject) {
                            final String query = stateEntry.getKey();
                            final var stateOverride = (Map<String, Object>) stateEntry.getValue();
                            final var propertyMap = BlockUtils.parseProperties(query);
                            assert propertyTypes.length == propertyMap.size();
                            long propertiesValue = 0;
                            for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
                                final byte keyIndex = findKeyIndexThrow(propertyTypes, entry.getKey(), null);
                                final byte valueIndex = findValueIndexThrow(propertyTypes[keyIndex], entry.getValue(), null);
                                propertiesValue = updateIndex(propertiesValue, keyIndex, valueIndex);
                            }

                            final RegistryData.BlockEntry entryOverride = RegistryData.block(namespace, RegistryData.Properties.fromMap(stateOverride), internCache, baseBlockEntry, properties);
                            final BlockImpl block = new BlockImpl(entryOverride,
                                    propertiesValue, null, null);
                            blockStateMap.set(block.stateId(), block);
                            propertiesKeys[propertiesOffset] = propertiesValue;
                            blocksValues[propertiesOffset++] = block;
                        }
                        possibleStates.set(blockId, new Long2ObjectArrayMap<>(propertiesKeys, blocksValues, propertiesOffset));
                    }
                    // Register default state
                    final int defaultState = properties.getInt("defaultStateId");
                    return blockStateMap.get(defaultState);
                });
        BLOCK_STATE_MAP = blockStateMap.toList();
        PROPERTIES_TYPE = propertiesType.toList();
        POSSIBLE_STATES = possibleStates.toList();
    }

    static @UnknownNullability Block get(String key) {
        return REGISTRY.get(Key.key(key));
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
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        assert propertyTypes != null;
        final byte keyIndex = findKeyIndexThrow(propertyTypes, property, this);
        final byte valueIndex = findValueIndexThrow(propertyTypes[keyIndex], value, this);
        final long updatedProperties = updateIndex(propertiesArray, keyIndex, valueIndex);
        return compute(updatedProperties);
    }

    @Override
    public Block withProperties(Map<String, String> properties) {
        if (properties.isEmpty()) return this;
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        assert propertyTypes != null;
        long updatedProperties = this.propertiesArray;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            final byte keyIndex = findKeyIndexThrow(propertyTypes, entry.getKey(), this);
            final byte valueIndex = findValueIndexThrow(propertyTypes[keyIndex], entry.getValue(), this);
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
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        assert propertyTypes != null;
        final int length = propertyTypes.length;
        if (length == 0) return Map.of();
        String[] keys = new String[length];
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            PropertyType property = propertyTypes[i];
            keys[i] = property.key();
            final long index = extractIndex(propertiesArray, i);
            values[i] = property.values().get((int) index);
        }
        return Object2ObjectMaps.unmodifiable(new Object2ObjectArrayMap<>(keys, values, length));
    }

    @Override
    public String state() {
        final Map<String, String> properties = properties();
        if (properties.isEmpty()) return name();
        StringBuilder builder = new StringBuilder(name()).append('[');
        boolean first = true;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (first) first = false;
            else builder.append(',');
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        builder.append(']');
        return builder.toString();
    }

    @Override
    public Block defaultState() {
        return Block.fromBlockId(id());
    }

    @Override
    public String getProperty(String property) {
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        final int length = propertyTypes.length;
        if (length == 0) return null;
        final int key = findKeyIndex(propertyTypes, property);
        if (key == -1) return null; // Property not found
        final long index = extractIndex(propertiesArray, key);
        return propertyTypes[key].values().get((int) index);
    }

    @Override
    public Collection<Block> possibleStates() {
        return Collection.class.cast(possibleProperties().values());
    }

    @Override
    public <T> @UnknownNullability T getTag(Tag<T> tag) {
        return tag.read(Objects.requireNonNullElse(nbt, CompoundBinaryTag.empty()));
    }

    private Long2ObjectArrayMap<BlockImpl> possibleProperties() {
        return POSSIBLE_STATES.get(id());
    }

    @Override
    public String toString() {
        return String.format("%s{properties=%s, nbt=%s, handler=%s}", name(), properties(), nbt, handler);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockImpl block)) return false;
        return stateId() == block.stateId() && Objects.equals(nbt, block.nbt) && Objects.equals(handler, block.handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stateId(), nbt, handler);
    }

    private Block compute(long updatedProperties) {
        if (updatedProperties == this.propertiesArray) return this;
        final BlockImpl block = possibleProperties().get(updatedProperties);
        assert block != null;
        // Reuse the same block instance if possible
        if (nbt == null && handler == null) return block;
        // Otherwise copy with the nbt and handler
        return new BlockImpl(block.registry(), block.propertiesArray, nbt, handler);
    }

    private static byte findKeyIndex(PropertyType[] properties, String key) {
        for (byte i = 0; i < properties.length; i++) {
            if (properties[i].key().equals(key)) return i;
        }
        return -1;
    }

    private static byte findValueIndex(PropertyType propertyType, String value) {
        final List<String> values = propertyType.values();
        return (byte) values.indexOf(value);
    }

    private static byte findKeyIndexThrow(PropertyType[] properties, String key, BlockImpl block) {
        final byte index = findKeyIndex(properties, key);
        if (index == -1) {
            if (block != null) {
                throw new IllegalArgumentException("Property " + key + " is not valid for block " + block);
            } else {
                throw new IllegalArgumentException("Unknown property key: " + key);
            }
        }
        return index;
    }

    private static byte findValueIndexThrow(PropertyType propertyType, String value, BlockImpl block) {
        final byte index = findValueIndex(propertyType, value);
        if (index == -1) {
            if (block != null) {
                throw new IllegalArgumentException("Property " + propertyType.key() + " value " + value + " is not valid for block " + block);
            } else {
                throw new IllegalArgumentException("Unknown property value: " + value);
            }
        }
        return index;
    }

    private record PropertyType(String key, List<String> values) {
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
