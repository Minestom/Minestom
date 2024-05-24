package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.collection.MergedMap;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

record BlockImpl(@NotNull Registry.BlockEntry registry,
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
    private static final ObjectArray<Block> BLOCK_STATE_MAP = ObjectArray.singleThread();
    // Block id -> valid property keys (order is important for lookup)
    private static final ObjectArray<PropertyType[]> PROPERTIES_TYPE = ObjectArray.singleThread();
    // Block id -> Map<PropertiesValues, Block>
    private static final ObjectArray<Long2ObjectArrayMap<BlockImpl>> POSSIBLE_STATES = ObjectArray.singleThread();
    private static final Registry.Container<Block> CONTAINER = Registry.createStaticContainer(Registry.Resource.BLOCKS, BlockImpl::createImpl);

    static {
        PROPERTIES_TYPE.trim();
        BLOCK_STATE_MAP.trim();
        POSSIBLE_STATES.trim();
    }

    static Block get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Block getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Block getId(int id) {
        return CONTAINER.getId(id);
    }

    static Block getState(int stateId) {
        return BLOCK_STATE_MAP.get(stateId);
    }

    static Collection<Block> values() {
        return CONTAINER.values();
    }

    @Override
    public @NotNull Block withProperty(@NotNull String property, @NotNull String value) {
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        assert propertyTypes != null;
        final byte keyIndex = findKeyIndex(propertyTypes, property, this);
        final byte valueIndex = findValueIndex(propertyTypes[keyIndex], value, this);
        final long updatedProperties = updateIndex(propertiesArray, keyIndex, valueIndex);
        return compute(updatedProperties);
    }

    @Override
    public @NotNull Block withProperties(@NotNull Map<@NotNull String, @NotNull String> properties) {
        if (properties.isEmpty()) return this;
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        assert propertyTypes != null;
        long updatedProperties = this.propertiesArray;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            final byte keyIndex = findKeyIndex(propertyTypes, entry.getKey(), this);
            final byte valueIndex = findValueIndex(propertyTypes[keyIndex], entry.getValue(), this);
            updatedProperties = updateIndex(updatedProperties, keyIndex, valueIndex);
        }
        return compute(updatedProperties);
    }

    @Override
    public @NotNull <T> Block withTag(@NotNull Tag<T> tag, @Nullable T value) {
        var builder = CompoundBinaryTag.builder();
        if (nbt != null) builder.put(nbt);
        tag.write(builder, value);
        final CompoundBinaryTag temporaryNbt = builder.build();
        final CompoundBinaryTag finalNbt = temporaryNbt.size() > 0 ? temporaryNbt : null;
        return new BlockImpl(registry, propertiesArray, finalNbt, handler);
    }

    @Override
    public @NotNull Block withNbt(@Nullable CompoundBinaryTag compound) {
        return new BlockImpl(registry, propertiesArray, compound, handler);
    }

    @Override
    public @NotNull Block withHandler(@Nullable BlockHandler handler) {
        return new BlockImpl(registry, propertiesArray, nbt, handler);
    }

    @Override
    public @Unmodifiable @NotNull Map<String, String> properties() {
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
    public @NotNull Block defaultState() {
        return Block.fromBlockId(id());
    }

    @Override
    public @NotNull Collection<@NotNull Block> possibleStates() {
        return Collection.class.cast(possibleProperties().values());
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
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

    private static Block createImpl(String namespace, Registry.Properties properties) {
        final int blockId = properties.getInt("id");
        final Registry.Properties stateObject = properties.section("states");

        // Retrieve properties
        PropertyType[] propertyTypes;
        {
            Registry.Properties stateProperties = properties.section("properties");
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
        PROPERTIES_TYPE.set(blockId, propertyTypes);

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
                    final byte keyIndex = findKeyIndex(propertyTypes, entry.getKey(), null);
                    final byte valueIndex = findValueIndex(propertyTypes[keyIndex], entry.getValue(), null);
                    propertiesValue = updateIndex(propertiesValue, keyIndex, valueIndex);
                }

                var mainProperties = Registry.Properties.fromMap(new MergedMap<>(stateOverride, properties.asMap()));
                final BlockImpl block = new BlockImpl(Registry.block(namespace, mainProperties),
                        propertiesValue, null, null);
                BLOCK_STATE_MAP.set(block.stateId(), block);
                propertiesKeys[propertiesOffset] = propertiesValue;
                blocksValues[propertiesOffset++] = block;
            }
            POSSIBLE_STATES.set(blockId, new Long2ObjectArrayMap<>(propertiesKeys, blocksValues, propertiesOffset));
        }
        // Register default state
        final int defaultState = properties.getInt("defaultStateId");
        return getState(defaultState);
    }

    private static byte findKeyIndex(PropertyType[] properties, String key, BlockImpl block) {
        for (byte i = 0; i < properties.length; i++) {
            if (properties[i].key().equals(key)) return i;
        }
        if (block != null) {
            throw new IllegalArgumentException("Property " + key + " is not valid for block " + block);
        } else {
            throw new IllegalArgumentException("Unknown property key: " + key);
        }
    }

    private static byte findValueIndex(PropertyType propertyType, String value, BlockImpl block) {
        final List<String> values = propertyType.values();
        final byte index = (byte) values.indexOf(value);
        if (index != -1) return index;
        if (block != null) {
            throw new IllegalArgumentException("Property " + propertyType.key() + " value " + value + " is not valid for block " + block);
        } else {
            throw new IllegalArgumentException("Unknown property value: " + value);
        }
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
