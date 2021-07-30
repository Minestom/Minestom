package net.minestom.server.instance.block;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.registry.Registry;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

final class BlockImpl implements Block {
    // Block state -> block object
    private static final Int2ObjectMap<Block> BLOCK_STATE_MAP = new Int2ObjectOpenHashMap<>();
    private static final Registry.Container<Block> CONTAINER = new Registry.Container<>(Registry.Resource.BLOCKS,
            (container, namespace, object) -> {
                final JsonObject stateObject = object.remove("states").getAsJsonObject();
                // Loop each state
                PropertyEntry propertyEntry = new PropertyEntry();
                for (var stateEntry : stateObject.entrySet()) {
                    final String query = stateEntry.getKey();
                    JsonObject stateOverride = stateEntry.getValue().getAsJsonObject();
                    final var propertyMap = BlockUtils.parseProperties(query);
                    final Block block = new BlockImpl(Registry.block(namespace, object, stateOverride),
                            propertyEntry, propertyMap, null, null);
                    BLOCK_STATE_MAP.put(block.stateId(), block);
                    propertyEntry.put(propertyMap, block);
                }
                // Register default state
                final int defaultState = object.get("defaultStateId").getAsInt();
                container.register(getState(defaultState));
            });
    private static final Cache<NBTCompound, NBTCompound> NBT_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .weakValues()
            .build();

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

    protected static class PropertyEntry extends ConcurrentHashMap<Map<String, String>, Block> {
    }

    private final Registry.BlockEntry registry;
    private final PropertyEntry propertyEntry;
    private final Map<String, String> properties;
    private final NBTCompound nbt;
    private final BlockHandler handler;

    BlockImpl(@NotNull Registry.BlockEntry registry,
              @NotNull PropertyEntry propertyEntry,
              @NotNull Map<String, String> properties,
              @Nullable NBTCompound nbt,
              @Nullable BlockHandler handler) {
        this.registry = registry;
        this.propertyEntry = propertyEntry;
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
        if (properties.isEmpty()) {
            return this;
        }
        if (this.properties.size() == properties.size()) {
            return compute(properties); // Map should be complete
        }
        var newProperties = new HashMap<>(this.properties);
        newProperties.putAll(properties);
        return compute(newProperties);
    }

    @Override
    public @NotNull <T> Block withTag(@NotNull Tag<T> tag, @Nullable T value) {
        var temporaryNbt = nbt != null ? nbt.deepClone() : new NBTCompound();
        tag.write(temporaryNbt, value);
        final var finalNbt = temporaryNbt.getSize() > 0 ? NBT_CACHE.get(temporaryNbt, Function.identity()) : null;
        return new BlockImpl(registry, propertyEntry, properties, finalNbt, handler);
    }

    @Override
    public @NotNull Block withHandler(@Nullable BlockHandler handler) {
        return new BlockImpl(registry, propertyEntry, properties, nbt, handler);
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
    public @NotNull Registry.BlockEntry registry() {
        return registry;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return nbt != null ? tag.read(nbt) : null;
    }

    private Block compute(Map<String, String> properties) {
        Block block = propertyEntry.get(properties);
        if (block == null)
            throw new IllegalArgumentException("Invalid properties: " + properties);
        return nbt == null && handler == null ? block :
                new BlockImpl(block.registry(), propertyEntry, block.properties(), nbt, handler);
    }
}
