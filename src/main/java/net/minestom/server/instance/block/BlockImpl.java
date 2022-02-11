package net.minestom.server.instance.block;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minestom.server.registry.Registry;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.ObjectArray;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.collection.MergedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private static final ObjectArray<Block> BLOCK_STATE_MAP = new ObjectArray<>();
    // Block id -> valid property keys (order is important for lookup)
    private static final ObjectArray<String[]> PROPERTIES_KEYS = new ObjectArray<>();
    // Block id -> [property key -> values]
    private static final ObjectArray<String[][]> PROPERTIES_VALUES = new ObjectArray<>();
    // Block id -> Map<PropertiesValues, Block>
    private static final ObjectArray<Map<PropertiesHolder, BlockImpl>> POSSIBLE_STATES = new ObjectArray<>();
    private static final Registry.Container<Block> CONTAINER = Registry.createContainer(Registry.Resource.BLOCKS,
            (namespace, properties) -> {
                final int blockId = properties.getInt("id");
                final Registry.Properties stateObject = properties.section("states");

                // Retrieve properties
                String[] keys = new String[0];
                String[][] values = new String[0][];
                {
                    Registry.Properties stateProperties = properties.section("properties");
                    if (stateProperties != null) {
                        final int stateCount = stateProperties.size();
                        keys = new String[stateCount];
                        values = new String[stateCount][];
                        int i = 0;
                        for (var entry : stateProperties) {
                            final int entryIndex = i++;
                            keys[entryIndex] = entry.getKey();

                            final var v = (List<String>) entry.getValue();
                            values[entryIndex] = v.toArray(String[]::new);
                        }
                    }
                }
                PROPERTIES_KEYS.set(blockId, keys);
                PROPERTIES_VALUES.set(blockId, values);

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
                        assert keys.length == propertyMap.size();
                        byte[] propertiesArray = new byte[keys.length];
                        for (var entry : propertyMap.entrySet()) {
                            final int keyIndex = ArrayUtils.indexOf(keys, entry.getKey());
                            if (keyIndex == -1) {
                                throw new IllegalArgumentException("Unknown property key: " + entry.getKey());
                            }
                            final byte valueIndex = (byte) ArrayUtils.indexOf(values[keyIndex], entry.getValue());
                            if (valueIndex == -1) {
                                throw new IllegalArgumentException("Unknown property value: " + entry.getValue());
                            }
                            propertiesArray[keyIndex] = valueIndex;
                        }

                        var mainProperties = Registry.Properties.fromMap(new MergedMap<>(properties.asMap(), stateOverride));
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
        PROPERTIES_KEYS.trim();
        PROPERTIES_VALUES.trim();
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
        final String[] keys = PROPERTIES_KEYS.get(id());
        final String[][] values = PROPERTIES_VALUES.get(id());
        assert keys != null;
        assert values != null;
        final int keyIndex = ArrayUtils.indexOf(keys, property);
        if (keyIndex == -1) {
            throw new IllegalArgumentException("Property " + property + " is not valid for block " + this);
        }
        final byte valueIndex = (byte) ArrayUtils.indexOf(values[keyIndex], value);
        if (valueIndex == -1) {
            throw new IllegalArgumentException("Value " + value + " is not valid for property " + property + " of block " + this);
        }
        var properties = this.propertiesArray.clone();
        properties[keyIndex] = valueIndex;
        return compute(properties);
    }

    @Override
    public @NotNull Block withProperties(@NotNull Map<@NotNull String, @NotNull String> properties) {
        if (properties.isEmpty()) return this;
        final String[] keys = PROPERTIES_KEYS.get(id());
        final String[][] values = PROPERTIES_VALUES.get(id());
        assert keys != null;
        assert values != null;
        byte[] result = this.propertiesArray.clone();
        for (var entry : properties.entrySet()) {
            final int keyIndex = ArrayUtils.indexOf(keys, entry.getKey());
            if (keyIndex == -1) {
                throw new IllegalArgumentException("Property " + entry.getKey() + " is not valid for block " + this);
            }
            final byte valueIndex = (byte) ArrayUtils.indexOf(values[keyIndex], entry.getValue());
            if (valueIndex == -1) {
                throw new IllegalArgumentException("Value " + entry.getValue() + " is not valid for property " + entry.getKey() + " of block " + this);
            }
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
    public @NotNull Block withHandler(@Nullable BlockHandler handler) {
        return new BlockImpl(registry, propertiesArray, nbt, handler);
    }

    @Override
    public @Unmodifiable @NotNull Map<String, String> properties() {
        final String[] keys = PROPERTIES_KEYS.get(id());
        final String[][] values = PROPERTIES_VALUES.get(id());
        assert keys != null;
        assert values != null;
        String[] finalValues = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            finalValues[i] = values[i][propertiesArray[i]];
        }
        return Map.class.cast(Object2ObjectMaps.unmodifiable(new Object2ObjectArrayMap<>(keys, finalValues, keys.length)));
    }

    @Override
    public @NotNull Collection<@NotNull Block> possibleStates() {
        return Collection.class.cast(possibleProperties().values());
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
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
        BlockImpl block = possibleProperties().get(new PropertiesHolder(properties));
        if (block == null)
            throw new IllegalArgumentException("Invalid properties: " + Arrays.toString(properties) + " for block " + this);
        return nbt == null && handler == null ? block : new BlockImpl(block.registry(), block.propertiesArray, nbt, handler);
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
