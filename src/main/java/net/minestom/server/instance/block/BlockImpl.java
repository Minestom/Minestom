package net.minestom.server.instance.block;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.registry.Registry;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.ObjectArray;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

final class BlockImpl implements Block {
    // Block state -> block object
    private static final ObjectArray<Block> BLOCK_STATE_MAP = new ObjectArray<>();
    private static final Registry.Container<Block> CONTAINER = new Registry.Container<>(Registry.Resource.BLOCKS,
            (container, namespace, object) -> {
                final var stateObject = (Map<String, Object>) object.get("states");
                // Loop each state
                var propertyEntry = new HashMap<Map<String, String>, Block>();
                AtomicReference<Map<Map<String, String>, Block>> ref = new AtomicReference<>();
                for (var stateEntry : stateObject.entrySet()) {
                    final String query = stateEntry.getKey();
                    final var stateOverride = (Map<String, Object>) stateEntry.getValue();
                    final var propertyMap = BlockUtils.parseProperties(query);
                    final Block block = new BlockImpl(Registry.block(namespace, object, stateOverride),
                            ref, propertyMap, null, null);
                    BLOCK_STATE_MAP.set(block.stateId(), block);
                    propertyEntry.put(propertyMap, block);
                }
                ref.setPlain(Map.copyOf(propertyEntry));
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

    private final Registry.BlockEntry registry;
    private final AtomicReference<Map<Map<String, String>, Block>> possibleProperties;
    private final Map<String, String> properties;
    private final NBTCompound nbt;
    private final BlockHandler handler;

    private int hashCode; // Cache

    BlockImpl(@NotNull Registry.BlockEntry registry,
              @NotNull AtomicReference<Map<Map<String, String>, Block>> possibleProperties,
              @NotNull Map<String, String> properties,
              @Nullable NBTCompound nbt,
              @Nullable BlockHandler handler) {
        this.registry = registry;
        this.possibleProperties = possibleProperties;
        this.properties = properties;
        this.nbt = nbt;
        this.handler = handler;
    }

    @Override
    public @NotNull Block withProperty(@NotNull String property, @NotNull String value) {
        var properties = new HashMap<>(this.properties);
        properties.replace(property, value);
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
        return new BlockImpl(registry, possibleProperties, properties, finalNbt, handler);
    }

    @Override
    public @NotNull Block withHandler(@Nullable BlockHandler handler) {
        return new BlockImpl(registry, possibleProperties, properties, nbt, handler);
    }

    @Override
    public boolean hasNbt() {
        return nbt != null;
    }

    @Override
    public @Nullable BlockHandler handler() {
        return handler;
    }

    @Override
    public @NotNull Map<String, String> properties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public @NotNull Collection<@NotNull Block> possibleStates() {
        return possibleProperties().values();
    }

    @Override
    public @NotNull Registry.BlockEntry registry() {
        return registry;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return nbt != null ? tag.read(nbt) : null;
    }

    private Map<Map<String, String>, Block> possibleProperties() {
        return possibleProperties.getPlain();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockImpl block = (BlockImpl) o;
        return stateId() == block.stateId() &&
                Objects.equals(nbt, block.nbt) &&
                Objects.equals(handler, block.handler);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(stateId(), nbt, handler);
            this.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return name() + "{" +
                "properties=" + properties +
                ", nbt=" + nbt +
                ", handler=" + handler +
                '}';
    }

    private Block compute(Map<String, String> properties) {
        if (this.properties.equals(properties)) return this;
        Block block = possibleProperties().get(properties);
        if (block == null)
            throw new IllegalArgumentException("Invalid properties: " + properties + " for block " + this);
        return nbt == null && handler == null ? block :
                new BlockImpl(block.registry(), possibleProperties, block.properties(), nbt, handler);
    }
}
