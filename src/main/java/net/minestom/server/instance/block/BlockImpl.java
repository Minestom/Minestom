package net.minestom.server.instance.block;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minestom.server.registry.Registry;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.collection.MergedMap;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

record BlockImpl(@NotNull Registry.BlockEntry registry,
                 byte @NotNull [] propertiesArray,
                 @Nullable NBTCompound nbt,
                 @Nullable BlockHandler handler) implements Block {
    // Block state -> block object
    private static final ObjectArray<Block> BLOCK_STATE_MAP = ObjectArray.singleThread();
    // Block id -> valid property keys (order is important for lookup)
    private static final ObjectArray<PropertyType[]> PROPERTIES_TYPE = ObjectArray.singleThread();
    // Block id -> Map<PropertiesValues, Block>
    private static final ObjectArray<Map<PropertiesHolder, BlockImpl>> POSSIBLE_STATES = ObjectArray.singleThread();
    private static final Registry.Container<Block> CONTAINER = Registry.createContainer(Registry.Resource.BLOCKS,
            (namespace, properties) -> {
                final int blockId = properties.getInt("id");
                final Registry.Properties stateObject = properties.section("states");

                // Retrieve properties
                PropertyType[] propertyTypes;
                {
                    Registry.Properties stateProperties = properties.section("properties");
                    if (stateProperties != null) {
                        final int stateCount = stateProperties.size();
                        propertyTypes = new PropertyType[stateCount];
                        int i = 0;
                        for (var entry : stateProperties) {
                            final var k = entry.getKey();
                            final var v = (List<String>) entry.getValue();
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
                    PropertiesHolder[] propertiesKeys = new PropertiesHolder[propertiesCount];
                    BlockImpl[] blocksValues = new BlockImpl[propertiesCount];
                    int propertiesOffset = 0;
                    for (var stateEntry : stateObject) {
                        final String query = stateEntry.getKey();
                        final var stateOverride = (Map<String, Object>) stateEntry.getValue();
                        final var propertyMap = BlockUtils.parseProperties(query);
                        assert propertyTypes.length == propertyMap.size();
                        byte[] propertiesArray = new byte[propertyTypes.length];
                        for (var entry : propertyMap.entrySet()) {
                            final byte keyIndex = findKeyIndex(propertyTypes, entry.getKey(), null);
                            final byte valueIndex = findValueIndex(propertyTypes[keyIndex], entry.getValue(), null);
                            propertiesArray[keyIndex] = valueIndex;
                        }

                        var mainProperties = Registry.Properties.fromMap(new MergedMap<>(stateOverride, properties.asMap()));
                        final BlockImpl block = new BlockImpl(Registry.block(namespace, mainProperties),
                                propertiesArray, null, null);
                        BLOCK_STATE_MAP.set(block.stateId(), block);
                        propertiesKeys[propertiesOffset] = new PropertiesHolder(propertiesArray);
                        blocksValues[propertiesOffset++] = block;
                    }
                    POSSIBLE_STATES.set(blockId, ArrayUtils.toMap(propertiesKeys, blocksValues, propertiesOffset));
                }
                // Register default state
                final int defaultState = properties.getInt("defaultStateId");
                return getState(defaultState);
            });
    private static final Cache<NBTCompound, NBTCompound> NBT_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .weakValues()
            .build();

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
        var properties = this.propertiesArray.clone();
        properties[keyIndex] = valueIndex;
        return compute(properties);
    }

    @Override
    public @NotNull Block withProperties(@NotNull Map<@NotNull String, @NotNull String> properties) {
        if (properties.isEmpty()) return this;
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        assert propertyTypes != null;
        byte[] result = this.propertiesArray.clone();
        for (var entry : properties.entrySet()) {
            final byte keyIndex = findKeyIndex(propertyTypes, entry.getKey(), this);
            final byte valueIndex = findValueIndex(propertyTypes[keyIndex], entry.getValue(), this);
            result[keyIndex] = valueIndex;
        }
        return compute(result);
    }

    @Override
    public @NotNull <T> Block withTag(@NotNull Tag<T> tag, @Nullable T value) {
        var temporaryNbt = new MutableNBTCompound(Objects.requireNonNullElse(nbt, NBTCompound.EMPTY));
        tag.write(temporaryNbt, value);
        final var finalNbt = temporaryNbt.getSize() > 0 ? NBT_CACHE.get(temporaryNbt.toCompound(), Function.identity()) : null;
        return new BlockImpl(registry, propertiesArray, finalNbt, handler);
    }

    @Override
    public @NotNull Block withNbt(@Nullable NBTCompound compound) {
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
            var property = propertyTypes[i];
            keys[i] = property.key();
            values[i] = property.values().get(propertiesArray[i]);
        }
        return Object2ObjectMaps.unmodifiable(new Object2ObjectArrayMap<>(keys, values, length));
    }

    @Override
    public @NotNull Collection<@NotNull Block> possibleStates() {
        return Collection.class.cast(possibleProperties().values());
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return tag.read(Objects.requireNonNullElse(nbt, NBTCompound.EMPTY));
    }

    private Map<PropertiesHolder, BlockImpl> possibleProperties() {
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

    private Block compute(byte[] properties) {
        if (Arrays.equals(propertiesArray, properties)) return this;
        final BlockImpl block = possibleProperties().get(new PropertiesHolder(properties));
        assert block != null;
        return nbt == null && handler == null ? block : new BlockImpl(block.registry(), block.propertiesArray, nbt, handler);
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

    private static final class PropertiesHolder {
        private final byte[] properties;
        private final int hashCode;

        public PropertiesHolder(byte[] properties) {
            this.properties = properties;
            this.hashCode = Arrays.hashCode(properties);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PropertiesHolder that)) return false;
            return Arrays.equals(properties, that.properties);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
