package net.minestom.server.instance.block;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.registry.Registry;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.ObjectArray;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

record BlockImpl(@NotNull Registry.BlockEntry registry,
                 @NotNull Map<String, String> properties,
                 @Nullable NBTCompound nbt,
                 @Nullable BlockHandler handler) implements Block {
    // Block state -> block object
    private static final ObjectArray<Block> BLOCK_STATE_MAP = new ObjectArray<>();
    // Block id -> Map<Properties, Block>
    private static final ObjectArray<Map<Map<String, String>, Block>> POSSIBLE_STATES = new ObjectArray<>();
    private static final Registry.Container<Block> CONTAINER = new Registry.Container<>(Registry.Resource.BLOCKS,
            (container, namespace, object) -> {
                final var stateObject = (Map<String, Object>) object.get("states");
                // Retrieve the block states
                {
                    final var stateEntries = stateObject.entrySet();
                    final int propertiesCount = stateEntries.size();
                    Map<String, String>[] propertiesKeys = new Map[propertiesCount];
                    Block[] blocksValues = new Block[propertiesCount];
                    int propertiesOffset = 0;
                    for (var stateEntry : stateEntries) {
                        final String query = stateEntry.getKey();
                        final var stateOverride = (Map<String, Object>) stateEntry.getValue();
                        final var propertyMap = BlockUtils.parseProperties(query);
                        final Block block = new BlockImpl(Registry.block(namespace, object, stateOverride),
                                propertyMap, null, null);
                        BLOCK_STATE_MAP.set(block.stateId(), block);
                        propertiesKeys[propertiesOffset] = propertyMap;
                        blocksValues[propertiesOffset++] = block;
                    }
                    POSSIBLE_STATES.set(((Number) object.get("id")).intValue(),
                            ArrayUtils.toMap(propertiesKeys, blocksValues, propertiesOffset));
                }
                // Register default state
                final int defaultState = ((Number) object.get("defaultStateId")).intValue();
                container.register(getState(defaultState));
            });
    private static final Cache<NBTCompound, NBTCompound> NBT_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .weakValues()
            .build();

    static {
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

    public BlockImpl {
        properties = Map.copyOf(properties);
    }

    @Override
    public @NotNull Block withProperty(@NotNull String property, @NotNull String value) {
        var properties = new HashMap<>(this.properties);
        final String oldProperty = properties.replace(property, value);
        if (oldProperty == null)
            throw new IllegalArgumentException("Property " + property + " does not exist");
        return compute(properties);
    }

    @Override
    public @NotNull Block withProperties(@NotNull Map<@NotNull String, @NotNull String> properties) {
        if (properties.isEmpty()) return this;
        if (this.properties.size() == properties.size()) {
            return compute(properties); // Map should be complete
        }
        var newProperties = new HashMap<>(this.properties);
        newProperties.putAll(properties);
        return compute(newProperties);
    }

    @Override
    public @NotNull <T> Block withTag(@NotNull Tag<T> tag, @Nullable T value) {
        var temporaryNbt = new MutableNBTCompound(Objects.requireNonNullElse(nbt, NBTCompound.EMPTY));
        tag.write(temporaryNbt, value);
        final var finalNbt = temporaryNbt.getSize() > 0 ? NBT_CACHE.get(temporaryNbt.toCompound(), Function.identity()) : null;
        return new BlockImpl(registry, properties, finalNbt, handler);
    }

    @Override
    public @NotNull Block withHandler(@Nullable BlockHandler handler) {
        return new BlockImpl(registry, properties, nbt, handler);
    }

    @Override
    public @NotNull Collection<@NotNull Block> possibleStates() {
        return possibleProperties().values();
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return nbt != null ? tag.read(nbt) : null;
    }

    private Map<Map<String, String>, Block> possibleProperties() {
        return POSSIBLE_STATES.get(id());
    }

    @Override
    public String toString() {
        return String.format("%s{properties=%s, nbt=%s, handler=%s}", name(), properties, nbt, handler);
    }

    private Block compute(Map<String, String> properties) {
        if (this.properties.equals(properties)) return this;
        Block block = possibleProperties().get(properties);
        if (block == null)
            throw new IllegalArgumentException("Invalid properties: " + properties + " for block " + this);
        return nbt == null && handler == null ? block : new BlockImpl(block.registry(), block.properties(), nbt, handler);
    }
}
